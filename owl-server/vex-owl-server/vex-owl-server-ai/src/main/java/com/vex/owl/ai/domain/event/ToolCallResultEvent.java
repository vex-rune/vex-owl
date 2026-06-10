package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 工具调用结果事件
 */
@Getter
@Setter
public class ToolCallResultEvent implements Serializable {

    private ToolCallRequestEvent.EventType eventType;
    private String toolCallId;
    private String toolName;
    private String result;
    private long timestamp;

    public ToolCallResultEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ToolCallResultEvent(ToolCallRequestEvent.EventType eventType, String toolCallId,
                               String toolName, String result) {
        this.eventType = eventType;
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
    }
}
