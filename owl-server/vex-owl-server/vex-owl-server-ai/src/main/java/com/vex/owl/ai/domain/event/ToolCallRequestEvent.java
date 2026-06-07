package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 工具调用请求事件
 */
@Getter
@Setter
public class ToolCallRequestEvent {

    private String tenantId;
    private String sessionId;
    private EventType eventType;
    private String toolCallId;
    private String toolName;
    private String arguments;
    private long timestamp;

    public ToolCallRequestEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ToolCallRequestEvent(String tenantId, String sessionId, EventType eventType,
                                 String toolCallId, String toolName, String arguments) {
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.arguments = arguments;
        this.timestamp = System.currentTimeMillis();
    }

    public enum EventType {
        BEFORE_EXECUTE,  // 工具执行前
        AFTER_EXECUTE    // 工具执行后
    }
}