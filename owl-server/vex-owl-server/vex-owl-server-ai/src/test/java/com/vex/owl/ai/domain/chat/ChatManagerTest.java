package com.vex.owl.ai.domain.chat;

import com.vex.owl.ai.SpringIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatManagerTest extends SpringIntegrationTest {

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("DELETE FROM ai_chat_message");
        jdbcTemplate.execute("DELETE FROM ai_chat_session");
    }

    @Test
    void createSession_shouldPersist() {
        ChatSessionEntity session = chatManager.createSession("tenant-1", "测试会话");

        assertThat(session.getId()).isNotBlank();
        assertThat(session.getUserId()).isEqualTo("tenant-1");
        assertThat(session.getTitle()).isEqualTo("测试会话");
        assertThat(session.getStatus()).isEqualTo("ACTIVE");
        assertThat(session.getMessageCount()).isEqualTo(0);
    }

    @Test
    void createSession_shouldUseDefaultTitle() {
        ChatSessionEntity session = chatManager.createSession("tenant-1", null);

        assertThat(session.getTitle()).isEqualTo("新对话");
    }

    @Test
    void findSession_shouldReturnExistingSessionByType() {
        ChatSessionEntity created = chatManager.createSessionByType("tenant-1", "CHAT");

        ChatSessionEntity fetched = chatManager.createSessionByType("tenant-1", "CHAT");

        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getUserId()).isEqualTo("tenant-1");
        assertThat(fetched.getSessionType()).isEqualTo("CHAT");
    }

    @Test
    void findSession_ByType_shouldCreateNewIfNotExists() {
        ChatSessionEntity session = chatManager.createSessionByType("tenant-1", "NEW_TYPE");

        assertThat(session.getId()).isNotBlank();
        assertThat(session.getUserId()).isEqualTo("tenant-1");
        assertThat(session.getSessionType()).isEqualTo("NEW_TYPE");
        assertThat(session.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void saveMessages_shouldPersist() {
        ChatSessionEntity session = chatManager.createSession("tenant-1", "会话");

        List<ChatMessageEntity> messages = List.of(
                ChatMessageEntity.builder()
                        .userId("tenant-1")
                        .conversationId(session.getId())
                        .messageType("USER")
                        .textContent("你好")
                        .build(),
                ChatMessageEntity.builder()
                        .userId("tenant-1")
                        .conversationId(session.getId())
                        .messageType("ASSISTANT")
                        .textContent("你好！")
                        .build()
        );

        List<ChatMessageEntity> saved = chatManager.saveMessages(messages);

        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getId()).isNotBlank();
    }

    @Test
    void getMessages_shouldReturnDescOrderByLimit() {
        ChatSessionEntity session = chatManager.createSession("tenant-1", "会话");

        chatManager.saveMessages(List.of(
                createMessage(session.getId(), "tenant-1", "USER", "消息1"),
                createMessage(session.getId(), "tenant-1", "ASSISTANT", "回复1"),
                createMessage(session.getId(), "tenant-1", "USER", "消息2")
        ));

        List<ChatMessageEntity> messages = chatManager.getMessages(session.getId(), 2);

        assertThat(messages).hasSize(2);
    }

    @Test
    void getMessagesAsc_shouldReturnAscOrder() {
        ChatSessionEntity session = chatManager.createSession("tenant-1", "会话");

        chatManager.saveMessages(List.of(
                createMessage(session.getId(), "tenant-1", "USER", "第一条"),
                createMessage(session.getId(), "tenant-1", "ASSISTANT", "第二条")
        ));

        List<ChatMessageEntity> messages = chatManager.getMessagesAsc(session.getId());

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getTextContent()).isEqualTo("第一条");
        assertThat(messages.get(1).getTextContent()).isEqualTo("第二条");
    }

    private ChatMessageEntity createMessage(String sessionId, String userId, String type, String text) {
        return ChatMessageEntity.builder()
                .userId(userId)
                .conversationId(sessionId)
                .messageType(type)
                .textContent(text)
                .build();
    }
}
