package com.vex.owl.ai.app.agent;

import com.vex.event.Event;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatMessageEntity;
import com.vex.owl.ai.domain.event.ChatMessageCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 聊天消息创建事件监听器
 *
 * <p>异步处理 ChatMessageCreateEvent，保存消息到数据库。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageCreateListener {

    private final ChatManager chatManager;

    @Async
    @EventListener
    @Transactional
    public void onEvent(Event<ChatMessageCreateEvent> event) {
        ChatMessageCreateEvent payload = event.getPayload();

        if (payload.getSessionId() == null || payload.getSessionId().isEmpty()) {
            log.warn("ChatMessageCreateEvent 缺少 sessionId，跳过保存");
            return;
        }

        try {
            ChatMessageEntity entity = ChatMessageEntity.builder()
                    .userId(payload.getUserId())
                    .conversationId(payload.getSessionId())
                    .messageType(payload.getMessageType())
                    .textContent(payload.getTextContent())
                    .build();
            chatManager.saveMessages(List.of(entity));
            log.debug("保存消息成功: sessionId={}, type={}", payload.getSessionId(), payload.getMessageType());
        } catch (Exception e) {
            log.warn("保存消息失败: sessionId={}", payload.getSessionId(), e);
        }
    }
}
