package com.vex.security.web.filter;

import com.vex.security.auth.AuthHeaderConstants;
import com.vex.event.CurrentTrace;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 链路追踪过滤器（所有 Filter 中最先执行）
 *
 * <p>sessionId 来源优先级：Cookie > Header > 新生成。
 * 无论来源如何，都会写入 Cookie 和 Header，确保下游一致。</p>
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE - 1)
public class TraceIdFilter implements WebFilter {

    private static final String COOKIE_SESSION_ID = "Vex-Session-Id";
    private static final int SESSION_MAX_AGE = 60 * 60 * 24 * 30; // 30 天

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String sid = resolveFromCookie(exchange);
        if (sid == null || sid.isEmpty()) {
            sid = resolveFromHeader(exchange);
        }
        if (sid == null || sid.isEmpty()) {
            sid = UUID.randomUUID().toString().replace("-", "");
        }
        final String sessionId = sid;
        final String traceId = UUID.randomUUID().toString().replace("-", "");

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(AuthHeaderConstants.HEADER_SESSION_ID, sessionId)
                .header(AuthHeaderConstants.HEADER_TRACE_ID, traceId)
                .build();

        log.debug("TraceIdFilter | sessionId={} | traceId={} | path={}",
                sessionId, traceId, request.getURI().getPath());

        ServerWebExchange mutated = exchange.mutate().request(request).build();

        mutated.getResponse().addCookie(
                ResponseCookie.from(COOKIE_SESSION_ID, sessionId)
                        .path("/")
                        .maxAge(SESSION_MAX_AGE)
                        .httpOnly(true)
                        .build());

        CurrentTrace trace = CurrentTrace.builder()
                .sessionId(sessionId)
                .traceId(traceId)
                .build();

        return chain.filter(mutated)
                .contextWrite(ctx -> ctx.put("trace.context", trace));
    }

    private static String resolveFromCookie(ServerWebExchange exchange) {
        org.springframework.http.HttpCookie cookie = exchange.getRequest().getCookies().getFirst(COOKIE_SESSION_ID);
        return cookie != null ? cookie.getValue() : null;
    }

    private static String resolveFromHeader(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(AuthHeaderConstants.HEADER_SESSION_ID);
        return header != null && !header.isEmpty() ? header : null;
    }
}