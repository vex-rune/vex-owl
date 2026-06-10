package com.vex.security.web;

import com.vex.event.CurrentUser;
import com.vex.event.CurrentTrace;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * 用户上下文持有器
 *
 * <p>基于 Reactor Context，整个请求链路（Controller → Service → Domain）都能拿到当前用户信息，
 * 无需层层传递 Header 参数。</p>
 *
 * <p>Context 分两层：</p>
 * <ul>
 *   <li>{@code trace.context} — TraceIdFilter 写入，仅含 sessionId + traceId</li>
 *   <li>{@code user.context} — AuthUserFilter 写入，含完整用户信息 + sessionId + traceId</li>
 * </ul>
 */
public final class UserContext {

    private static final String TRACE_KEY = "trace.context";
    private static final String USER_KEY = "user.context";

    public static Mono<Void> put(CurrentUser user) {
        return Mono.deferContextual(ctx ->
                Mono.<Void>empty().contextWrite(Context.of(USER_KEY, user))
        );
    }

    public static CurrentUser current() {
        try {
            return Mono.deferContextual(ctx -> {
                CurrentUser user = ctx.hasKey(USER_KEY) ? ctx.get(USER_KEY) : null;
                if (user != null) return Mono.just(user);
                CurrentTrace trace = ctx.hasKey(TRACE_KEY) ? ctx.get(TRACE_KEY) : null;
                if (trace != null) {
                    return Mono.just(CurrentUser.builder()
                            .sessionId(trace.getSessionId())
                            .traceId(trace.getTraceId())
                            .build());
                }
                return Mono.just(CurrentUser.anonymous());
            }).contextWrite(Context.empty()).block();
        } catch (Exception e) {
            return CurrentUser.anonymous();
        }
    }

    public static Mono<CurrentUser> currentMono() {
        return Mono.deferContextual(ctx -> {
            CurrentUser user = ctx.hasKey(USER_KEY) ? ctx.get(USER_KEY) : null;
            if (user != null) return Mono.just(user);
            CurrentTrace trace = ctx.hasKey(TRACE_KEY) ? ctx.get(TRACE_KEY) : null;
            if (trace != null) {
                return Mono.just(CurrentUser.builder()
                        .sessionId(trace.getSessionId())
                        .traceId(trace.getTraceId())
                        .build());
            }
            return Mono.just(CurrentUser.anonymous());
        });
    }

    public static String getUserId() {
        return current().getUserId();
    }

    public static String getUserName() {
        return current().getUserName();
    }

    public static String getSessionId() {
        CurrentUser u = current();
        String sid = u.getSessionId();
        if (sid != null && !sid.isEmpty()) return sid;
        return currentFromTrace().getSessionId();
    }

    public static String getTraceId() {
        CurrentUser u = current();
        String tid = u.getTraceId();
        if (tid != null && !tid.isEmpty()) return tid;
        return currentFromTrace().getTraceId();
    }

    private static CurrentTrace currentFromTrace() {
        try {
            return Mono.deferContextual(ctx -> {
                if (ctx.hasKey(TRACE_KEY)) {
                    return Mono.just(ctx.get(TRACE_KEY));
                }
                return Mono.just(CurrentTrace.anonymous());
            }).contextWrite(Context.empty()).block();
        } catch (Exception e) {
            return CurrentTrace.anonymous();
        }
    }
}