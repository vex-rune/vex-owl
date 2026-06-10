package com.vex.security.web.filter;

import com.vex.event.CurrentUser;
import com.vex.event.CurrentTrace;
import com.vex.event.CurrentUserResolver;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Optional;

/**
 * CurrentUserResolver 实现（由 web 模块注册）
 *
 * <p>从 Reactor Context 中读取 CurrentUser / CurrentTrace。</p>
 */
public class CurrentUserResolverImpl implements CurrentUserResolver {

    private static final String TRACE_KEY = "trace.context";
    private static final String USER_KEY = "user.context";

    @Override
    public Optional<CurrentUser> resolveCurrentUser() {
        try {
            CurrentUser user = Mono.deferContextual(ctx -> {
                CurrentUser u = ctx.hasKey(USER_KEY) ? ctx.get(USER_KEY) : null;
                if (u != null) return Mono.just(u);
                CurrentTrace trace = ctx.hasKey(TRACE_KEY) ? ctx.get(TRACE_KEY) : null;
                if (trace != null) {
                    return Mono.just(CurrentUser.builder()
                            .sessionId(trace.getSessionId())
                            .traceId(trace.getTraceId())
                            .build());
                }
                return Mono.just(CurrentUser.anonymous());
            }).contextWrite(Context.empty()).block();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.of(CurrentUser.anonymous());
        }
    }

    @Override
    public Optional<CurrentTrace> resolveCurrentTrace() {
        try {
            CurrentTrace trace = Mono.deferContextual(ctx -> {
                if (ctx.hasKey(TRACE_KEY)) {
                    return Mono.just(ctx.get(TRACE_KEY));
                }
                return Mono.just(CurrentTrace.anonymous());
            }).contextWrite(Context.empty()).block();
            return Optional.ofNullable(trace);
        } catch (Exception e) {
            return Optional.of(CurrentTrace.anonymous());
        }
    }
}