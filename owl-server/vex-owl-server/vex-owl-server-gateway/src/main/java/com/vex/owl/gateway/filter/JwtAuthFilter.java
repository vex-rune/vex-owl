package com.vex.owl.gateway.filter;

import com.vex.owl.gateway.config.JwtConfig;
import com.vex.security.jwt.JwtClaimConstants;
import com.vex.security.auth.AuthHeaderConstants;
import com.vex.security.auth.AuthHeaders;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;

    private static final List<String> WHITE_LIST = List.of(
            "/api/user/auth/login",
            "/api/user/auth/register",
            "/api/user/auth/send/register/code",
            "/api/user/auth/send/login/code",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals( request.getMethod())) {
            return chain.filter(exchange);
        }

        if (isWhiteListPath(path)) {
            log.debug("白名单路径，跳过JWT验证: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(jwtConfig.getHeader());

        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            log.warn("缺少有效的Authorization头: {}", path);
            return onError(exchange, "未授权访问，请先登录", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(jwtConfig.getPrefix().length());

        SecretKey secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            AuthHeaders authHeaders = buildAuthHeaders(claims);
            Map<String, String> headerMap = authHeaders.toHeaders();

            ServerHttpRequest.Builder requestBuilder = request.mutate()
                    .header(AuthHeaderConstants.HEADER_AUTH_ENABLED, headerMap.get(AuthHeaderConstants.HEADER_AUTH_ENABLED))
                    .header(AuthHeaderConstants.HEADER_USER_ID, headerMap.get(AuthHeaderConstants.HEADER_USER_ID))
                    .header(AuthHeaderConstants.HEADER_USER_NAME, headerMap.get(AuthHeaderConstants.HEADER_USER_NAME))
                    .header(AuthHeaderConstants.HEADER_USER_GROUP, headerMap.get(AuthHeaderConstants.HEADER_USER_GROUP))
                    .header(AuthHeaderConstants.HEADER_LOGIN_TIME, headerMap.get(AuthHeaderConstants.HEADER_LOGIN_TIME))
                    .header(AuthHeaderConstants.HEADER_ROLE, headerMap.get(AuthHeaderConstants.HEADER_ROLE))
                    .header(AuthHeaderConstants.HEADER_EMAIL, headerMap.get(AuthHeaderConstants.HEADER_EMAIL))
                    .header(AuthHeaderConstants.HEADER_NICKNAME, headerMap.get(AuthHeaderConstants.HEADER_NICKNAME))
                    .header("X-Gateway-Filter", "JwtAuthFilter");

            log.debug("JWT验证成功 | 用户ID: {} | 路径: {}", authHeaders.getUserId(), path);

            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());

        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期 | 路径: {} | 错误: {}", path, e.getMessage());
            return onError(exchange, "令牌已过期，请重新登录", HttpStatus.UNAUTHORIZED);
        } catch (JwtException e) {
            log.warn("JWT令牌验证失败 | 路径: {} | 错误: {}", path, e.getMessage());
            return onError(exchange, "无效的令牌", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("JWT处理异常 | 路径: {} | 错误: {}", path, e.getMessage(), e);
            return onError(exchange, "认证失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private AuthHeaders buildAuthHeaders(Claims claims) {
        Map<String, Object> claimsMap = claims;

        String userId = "";
        String userName = "";
        String userGroup = AuthHeaderConstants.DEFAULT_USER_GROUP;
        String loginTime = AuthHeaderConstants.DEFAULT_LOGIN_TIME;
        String role = "";
        String email = "";
        String nickname = "";

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_USER_ID)) {
            userId = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_USER_ID));
        } else {
            userId = claims.getSubject();
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_USER_NAME)) {
            userName = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_USER_NAME));
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_USER_GROUP)) {
            userGroup = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_USER_GROUP));
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_LOGIN_TIME)) {
            loginTime = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_LOGIN_TIME));
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_ROLE)) {
            role = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_ROLE));
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_EMAIL)) {
            email = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_EMAIL));
        }

        if (claimsMap.containsKey(JwtClaimConstants.CLAIM_NICKNAME)) {
            nickname = String.valueOf(claimsMap.get(JwtClaimConstants.CLAIM_NICKNAME));
        }

        return AuthHeaders.builder()
                .authEnabled(true)
                .userId(userId)
                .userName(userName)
                .userGroup(userGroup)
                .loginTime(loginTime)
                .role(role)
                .email(email)
                .nickname(nickname)
                .build();
    }

    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}",
                status.value(), message, System.currentTimeMillis());

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}