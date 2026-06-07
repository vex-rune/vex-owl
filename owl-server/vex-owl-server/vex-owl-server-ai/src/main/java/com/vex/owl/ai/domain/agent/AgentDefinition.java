package com.vex.owl.ai.domain.agent;

/**
 * Agent 定义
 *
 * <p>描述 Agent 的元数据，用于注册和查找</p>
 */
public record AgentDefinition(
        String name,
        String description
) {

    /**
     * 创建 Agent 定义
     */
    public static AgentDefinition of(String name, String description) {
        return new AgentDefinition(name, description);
    }
}
