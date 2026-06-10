package com.vex.security.web;

import com.vex.security.auth.AuthHeaders;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * 请求级用户上下文持有器
 *
 * <p>基于 Reactor Context，整个请求链路（Controller → Service → Domain）都能拿到当前用户信息，
 * 无需层层传递 Header 参数。</p>
 */
public final class RequestUserHolder {

    private static final String CONTEXT_KEY = "auth.headers";

    public static Mono<Void> put(AuthHeaders authHeaders) {
        return Mono.deferContextual(ctx ->
                Mono.<Void>empty().contextWrite(Context.of(CONTEXT_KEY, authHeaders))
        );
    }

    public static AuthHeaders current() {
        try {
            return Mono.deferContextual(ctx -> {
                if (ctx.hasKey(CONTEXT_KEY)) {
                    return Mono.just(ctx.get(CONTEXT_KEY));
                }
                return Mono.just(AuthHeaders.anonymous());
            }).contextWrite(Context.empty()).block();
        } catch (Exception e) {
            return AuthHeaders.anonymous();
        }
    }

    public static Mono<AuthHeaders> currentMono() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(CONTEXT_KEY)) {
                return Mono.just(ctx.get(CONTEXT_KEY));
            }
            return Mono.just(AuthHeaders.anonymous());
        });
    }

    public static String getUserId() {
        return current().getUserId();
    }

    public static String getUserName() {
        return current().getUserName();
    }

    public static String getSessionId() {
        return current().getSessionId();
    }

    public static String getTraceId() {
        return current().getTraceId();
    }
}
