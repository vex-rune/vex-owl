package com.vex.owl.ai.app.tools;

import java.util.Collections;
import java.util.List;

import org.springframework.ai.tool.ToolCallback;

/**
 * 工具回调服务
 * <p>按租户管理本地Tool和MCP Tool可用列表，预留扩展点</p>
 */
public class ToolCallbacksServer {

    /**
     * 获取当前租户可用的本地Tool列表
     *
     * @param tenantId 租户ID
     * @return 本地ToolCallback列表，预留实现返回空集合
     */
    public List<ToolCallback> getAvailableLocalTools(String tenantId) {
        return Collections.emptyList();
    }

    public List<ToolCallback> getAvailableMcpTools(String tenantId) {
        return Collections.emptyList();
    }
}
