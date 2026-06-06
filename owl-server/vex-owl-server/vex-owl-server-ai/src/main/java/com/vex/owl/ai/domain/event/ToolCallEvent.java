package com.vex.owl.ai.domain.event;

public record ToolCallEvent(
    // 工具
    String toolName,
    String input,
    String output,

    // 你存在 ToolContext 里的
    String tenantId,
    String sessionId,
    String msgId,

    long timestamp
) {}