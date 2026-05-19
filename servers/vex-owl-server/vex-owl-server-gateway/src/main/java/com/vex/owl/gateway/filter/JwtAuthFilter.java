 package com.vex.gateway.filter;

import com.vex.gateway.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * JWT认证全局过滤器
 * 对所有请求进行JWT令牌验证，拦截未认证的请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    /**
     * 白名单路径，无需JWT认证
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/user/login",
            "/api/user/register",
            "/actuator/health",
            "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 检查是否在白名单中
        if (isWhiteListPath(path)) {
            log.debug("白名单路径，跳过JWT验证: {}", path);
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(jwtConfig.getHeader());
        
        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            log.warn("缺少有效的Authorization头: {}", path);
            return onError(exchange, "未授权访问，请先登录", HttpStatus.UNAUTHORIZED);
        }

        // 提取JWT令牌
        String token = authHeader.substring(jwtConfig.getPrefix().length());

        try {
            // 验证并解析JWT
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 将用户信息添加到请求头中，传递给下游服务
            String userId = claims.getSubject();
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Claims", claims.toString())
                    .header("X-Gateway-Filter", "JwtAuthFilter")
                    .build();

            log.debug("JWT验证成功 | 用户ID: {} | 路径: {}", userId, path);
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

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

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 错误响应处理
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"timestamp\":%d}",
                status.value(), message, System.currentTimeMillis());
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        // 设置过滤器优先级，确保在其他过滤器之前执行
        return -100;
    }
}
