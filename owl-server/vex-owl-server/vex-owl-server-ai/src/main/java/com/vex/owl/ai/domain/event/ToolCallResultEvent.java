package com.vex.owl.ai.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.messages.ToolResponseMessage;

/**
 * 工具调用结果事件
 */
@Getter
@Setter
public class ToolCallResultEvent {

    private String tenantId;
    private String sessionId;
    private ToolCallRequestEvent.EventType eventType;
    private ToolResponseMessage.ToolResponse data;
    private long timestamp;

    public ToolCallResultEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public ToolCallResultEvent(String tenantId, String sessionId,
                            ToolCallRequestEvent.EventType eventType,
                            ToolResponseMessage.ToolResponse data) {
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
}