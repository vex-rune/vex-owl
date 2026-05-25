package com.vex.owl.ai.domain.mcp;

/**
 * MCP 租户上下文持有者
 * <p>基于 ThreadLocal 在 MCP 请求处理期间传递租户ID。
 * 由 WebFilter 在请求入口设置，在请求结束时清理。</p>
 */
public final class McpTenantContextHolder {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private McpTenantContextHolder() {
    }

    /**
     * 设置当前请求的租户ID
     *
     * @param tenantId 租户ID
     */
    public static void set(String tenantId) {
        CONTEXT.set(tenantId);
    }

    /**
     * 获取当前请求的租户ID
     *
     * @return 租户ID，未设置时返回 null
     */
    public static String get() {
        return CONTEXT.get();
    }

    /**
     * 清理上下文，必须在请求结束时调用
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
