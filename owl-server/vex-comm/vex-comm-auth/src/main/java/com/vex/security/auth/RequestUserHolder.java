package com.vex.security.auth;

/**
 * Reactor 核心类型：
 * - Mono&lt;T&gt;：响应式流的单值容器，代表 0 或 1 个元素的异步序列。
 *   类似 Optional&lt;T&gt;，但支持链式操作和非阻塞调度。
 * - Context：Reactor 的请求级上下文，沿着响应式链路向下传递，
 *   类似 ThreadLocal 但适配 WebFlux 的多线程模型。
 *   通过 contextWrite 写入，Mono.deferContextual 读取。
 */
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * 请求级用户上下文持有器
 *
 * <p>基于 Reactor Context，整个请求链路（Controller → Service → Domain）都能拿到当前用户信息，
 * 无需层层传递 Header 参数。</p>
 *
 * <p>使用方式：</p>
 * <pre>
 * // 在任何地方获取当前用户
 * AuthHeaders user = RequestUserHolder.current();
 * String tenantId = user.getUserId();
 *
 * // 响应式链路中获取
 * RequestUserHolder.currentMono()
 *     .map(AuthHeaders::getUserId)
 *     .subscribe(tenantId -> ...);
 * </pre>
 */
public final class RequestUserHolder {

    private static final String CONTEXT_KEY = "auth.headers";


    /**
     * 在 Reactor Context 中设置当前用户信息（由 Filter 调用）
     */
    public static Mono<Void> put(AuthHeaders authHeaders) {
        return Mono.deferContextual(ctx ->
                Mono.<Void>empty().contextWrite(Context.of(CONTEXT_KEY, authHeaders))
        );
    }

    /**
     * 获取当前请求的用户信息（非响应式，适用于 @EventListener 等同步场景）
     */
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

    /**
     * 从 Reactor Context 获取当前用户（响应式链路中使用）
     */
    public static Mono<AuthHeaders> currentMono() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(CONTEXT_KEY)) {
                return Mono.just(ctx.get(CONTEXT_KEY));
            }
            return Mono.just(AuthHeaders.anonymous());
        });
    }

    /**
     * 获取当前用户ID（快捷方法）
     */
    public static String getUserId() {
        return current().getUserId();
    }

    /**
     * 获取当前用户名（快捷方法）
     */
    public static String getUserName() {
        return current().getUserName();
    }
}
