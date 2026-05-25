package com.vex.owl.ai.domain.mcp;

/**
 * MCP 租户权限检查器
 * <p>判断指定租户是否拥有 MCP 功能的访问权限。
 * 不同租户可能有不同的 MCP 套餐，并非所有租户都开放了 MCP。</p>
 *
 * <h3>典型实现</h3>
 * <ul>
 *   <li>查数据库：{@code SELECT mcp_enabled FROM tenant WHERE tenant_id = ?}</li>
 *   <li>查 Redis：{@code redis.get("mcp:access:" + tenantId)}</li>
 *   <li>查配置中心：按租户ID读取功能开关</li>
 * </ul>
 */
@FunctionalInterface
public interface McpTenantPermissionChecker {

    /**
     * 检查租户是否有 MCP 权限
     *
     * @param tenantId 租户ID
     * @return true 表示允许使用 MCP 工具，false 表示拒绝
     */
    boolean hasMcpAccess(String tenantId);
}
