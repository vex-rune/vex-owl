package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 聊天消息创建事件
 */
@Getter
@Setter
public class ChatMessageCreateEvent implements Serializable {

    private String userId;
    private String sessionId;
    private String messageType;
    private String textContent;
    private long timestamp;

    public ChatMessageCreateEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ChatMessageCreateEvent(String userId, String sessionId, String messageType, String textContent) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.textContent = textContent;
        this.timestamp = System.currentTimeMillis();
    }
}