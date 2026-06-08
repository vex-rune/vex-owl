package com.vex.security.auth.filter;

import com.vex.security.auth.AuthHeaderConstants;
import com.vex.security.auth.AuthHeaders;
import com.vex.security.auth.RequestUserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 从 Gateway 转发的 Header 中提取用户信息，注入 Reactor Context
 *
 * <p>所有下游代码可通过 {@link RequestUserHolder#current()} 获取当前用户，
 * 无需逐层传递 @RequestHeader 参数。</p>
 *
 * <p>由 {@link AuthUserFilterAutoConfiguration} 自动注册，无需手动配置。</p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthUserFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String userId = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_USER_ID);
        String userName = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_USER_NAME);
        String userGroup = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_USER_GROUP);
        String loginTime = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_LOGIN_TIME);
        String role = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_ROLE);
        String email = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_EMAIL);
        String nickname = request.getHeaders().getFirst(AuthHeaderConstants.HEADER_NICKNAME);

        AuthHeaders authHeaders = AuthHeaders.builder()
                .authEnabled(userId != null && !userId.isBlank())
                .userId(userId != null ? userId : "")
                .userName(userName != null ? userName : "")
                .userGroup(userGroup != null ? userGroup : AuthHeaderConstants.DEFAULT_USER_GROUP)
                .loginTime(loginTime != null ? loginTime : AuthHeaderConstants.DEFAULT_LOGIN_TIME)
                .role(role != null ? role : "")
                .email(email != null ? email : "")
                .nickname(nickname != null ? nickname : "")
                .build();

        log.debug("AuthUserFilter | userId={} | path={}", authHeaders.getUserId(), request.getURI().getPath());

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put("auth.headers", authHeaders));
    }
}
