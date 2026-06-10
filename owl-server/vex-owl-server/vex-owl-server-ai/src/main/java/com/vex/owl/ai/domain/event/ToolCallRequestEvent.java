package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工具调用请求事件
 */
@Getter
@Setter
public class ToolCallRequestEvent implements Serializable {

    private EventType eventType;
    private String toolCallId;
    private String toolName;
    private String arguments;
    private long timestamp;

    public ToolCallRequestEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ToolCallRequestEvent(EventType eventType, String toolCallId, String toolName, String arguments) {
        this.eventType = eventType;
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.arguments = arguments;
        this.timestamp = System.currentTimeMillis();
    }

    public enum EventType {
        BEFORE_EXECUTE,
        AFTER_EXECUTE
    }
}
