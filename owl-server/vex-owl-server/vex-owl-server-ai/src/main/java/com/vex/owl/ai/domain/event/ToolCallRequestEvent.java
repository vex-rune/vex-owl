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

    private String userId;
    private String sessionId;
    private String provider;
    private String modelName;
    private String toolCallId;
    private String toolName;
    private String arguments;
    private long timestamp;

    public ToolCallRequestEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ToolCallRequestEvent(String userId, String sessionId, String provider, String modelName,
                                 String toolCallId, String toolName, String arguments) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.toolCallId = toolCallId;
        this.toolName = toolName;
        this.arguments = arguments;
        this.timestamp = System.currentTimeMillis();
    }
}