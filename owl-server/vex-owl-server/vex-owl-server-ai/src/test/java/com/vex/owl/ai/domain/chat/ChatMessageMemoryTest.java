package com.vex.owl.ai.domain.chat;

import com.vex.owl.ai.SpringIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ChatMessageMemoryTest extends SpringIntegrationTest {

    @Autowired
    private ChatMessageMemory chatMessageMemory;

    @Autowired
    private ChatManager chatManager;

    private ChatSessionEntity createSession(String userId) {
        return chatManager.createSession(userId, "测试会话");
    }

    private ChatMessageEntity createMessage(String sessionId, String userId, String type, String text) {
        return ChatMessageEntity.builder()
                .userId(userId)
                .conversationId(sessionId)
                .messageType(type)
                .textContent(text)
                .build();
    }

    @Test
    void add_shouldSaveMessages() {
        ChatSessionEntity session = createSession("tenant-1");

        List<Message> messages = List.of(
                new UserMessage("你好"),
                new AssistantMessage("你好！")
        );

        chatMessageMemory.add(session.getId(), messages);

        List<ChatMessageEntity> saved = chatManager.getMessagesAsc(session.getId());
        assertThat(saved).hasSize(2);
    }

    @Test
    void get_shouldReturnLastNMessages() {
        ChatSessionEntity session = createSession("tenant-1");

        for (int i = 1; i <= 10; i++) {
            chatManager.saveMessages(List.of(
                    createMessage(session.getId(), "tenant-1", "USER", "消息" + i)
            ));
        }

        List<Message> messages = chatMessageMemory.get(session.getId());

        assertThat(messages).hasSize(10);
    }

    @Test
    void get_shouldConvertUserMessage() {
        ChatSessionEntity session = createSession("tenant-1");

        chatMessageMemory.add(session.getId(), List.of(new UserMessage("测试用户消息")));

        List<Message> messages = chatMessageMemory.get(session.getId());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(UserMessage.class);
        assertThat(messages.get(0).getText()).isEqualTo("测试用户消息");
    }

    @Test
    void get_shouldConvertAssistantMessage() {
        ChatSessionEntity session = createSession("tenant-1");

        chatMessageMemory.add(session.getId(), List.of(new AssistantMessage("测试助手回复")));

        List<Message> messages = chatMessageMemory.get(session.getId());

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).isInstanceOf(AssistantMessage.class);
        assertThat(messages.get(0).getText()).isEqualTo("测试助手回复");
    }

    @Test
    void get_shouldIgnoreToolMessages() {
        ChatSessionEntity session = createSession("tenant-1");

        chatManager.saveMessages(List.of(
                createMessage(session.getId(), "tenant-1", "TOOL", "工具结果")
        ));

        List<Message> messages = chatMessageMemory.get(session.getId());

        assertThat(messages).isEmpty();
    }

    @Test
    void get_shouldReturnEmptyForNonExistentSession() {
        List<Message> messages = chatMessageMemory.get("non-existent-session");

        assertThat(messages).isEmpty();
    }

    @Test
    void get_shouldRespectOrder() {
        ChatSessionEntity session = createSession("tenant-1");

        chatManager.saveMessages(List.of(createMessage(session.getId(), "tenant-1", "USER", "第一条")));
        chatManager.saveMessages(List.of(createMessage(session.getId(), "tenant-1", "ASSISTANT", "第二条")));
        chatManager.saveMessages(List.of(createMessage(session.getId(), "tenant-1", "USER", "第三条")));

        List<Message> messages = chatMessageMemory.get(session.getId());

        assertThat(messages).hasSize(3);
        // getMessages DESC 顺序（最新的在前）
        assertThat(messages.get(0).getText()).isEqualTo("第三条");
        assertThat(messages.get(1).getText()).isEqualTo("第二条");
        assertThat(messages.get(2).getText()).isEqualTo("第一条");
    }
}
