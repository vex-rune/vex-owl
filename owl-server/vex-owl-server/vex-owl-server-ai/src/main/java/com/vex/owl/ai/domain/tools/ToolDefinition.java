package com.vex.owl.ai.domain.tools;

/**
 * 工具定义
 *
 * <p>描述工具的元数据，用于注册和查找</p>
 */
public record ToolDefinition(
        String name,
        String description
) {
    public static ToolDefinition of(String name, String description) {
        return new ToolDefinition(name, description);
    }
}
