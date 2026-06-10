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
        return Mono.deferContextual(ctx -> ctx.<Mono<CurrentUser>>get(USER_KEY)).blockOptional();
    }
    @Override
    public Optional<CurrentTrace> resolveCurrentTrace() {
        return Mono.deferContextual(ctx -> ctx.<Mono<CurrentTrace>>get(TRACE_KEY)).blockOptional();
    }
}