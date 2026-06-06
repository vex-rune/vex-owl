package com.vex.owl.ai.domain.chat;

import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 对话管理器
 *
 * <p>整合会话管理和消息管理，提供统一的对话能力</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatManager {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final EntityManager entityManager;

    // ==================== 会话管理 ====================

    /**
     * 创建新会话
     */
    @Transactional
    public ChatSessionEntity createSession(String tenantId, String title) {
        ChatSessionEntity session = ChatSessionEntity.builder()
                .tenantId(tenantId)
                .title(title != null ? title : "新对话")
                .status("ACTIVE")
                .messageCount(0)
                .pinned(false)
                .starred(false)
                .build();

        return sessionRepository.save(session);
    }

    /**
     * 查询租户会话列表（分页）
     */
    public Page<ChatSessionEntity> getSessions(String tenantId, int page, int size) {
        return sessionRepository.findByTenantIdAndStatusNotOrderByPinnedDescCreateTimeDesc(
                tenantId, "DELETED", org.springframework.data.domain.PageRequest.of(page, size));
    }

    /**
     * 获取会话详情(如果没有会创建)
     */
    @Transactional
    public ChatSessionEntity getSession(String sessionId, String tenantId) {
        if (sessionRepository.existsById(sessionId)) {
            return sessionRepository.findByIdAndTenantId(sessionId, tenantId)
                    .orElseThrow(() -> new ChatException("会话不存在: " + sessionId));
        }
        ChatSessionEntity chatSessionEntity = new ChatSessionEntity();
        chatSessionEntity.setId(sessionId);
        chatSessionEntity.setTenantId(tenantId);
        chatSessionEntity.setTitle("新对话");
        chatSessionEntity.setStatus("ACTIVE");
        chatSessionEntity.setMessageCount(0);
        chatSessionEntity.setPinned(false);
        chatSessionEntity.setStarred(false);
        return sessionRepository.save(chatSessionEntity);
    }


    // ==================== 消息管理 ====================

    /**
     * 批量保存消息
     */
    @Transactional
    public List<ChatMessageEntity> saveMessages(List<ChatMessageEntity> messages) {
        List<ChatMessageEntity> saved = messageRepository.saveAll(messages);
        return saved;
    }

    /**
     * 查询会话消息历史（倒序）
     */
    public List<ChatMessageEntity> getMessages(String sessionId, int limit) {
        return messageRepository.findByConversationIdOrderByCreateTimeDesc(sessionId, PageRequest.of(0, limit));
    }

    /**
     * 查询会话消息历史（正序，用于客户端展示）
     */
    public List<ChatMessageEntity> getMessagesAsc(String sessionId) {
        return messageRepository.findByConversationIdOrderByCreateTimeAsc(sessionId);
    }

    /**
     * 通用消息查询
     */
    public List<ChatMessageEntity> queryMessages(QueriesPageRequest request) {
        log.debug("对话消息通用查询, request: {}", request);
        return JpaQueriesExecutor.of(ChatMessageEntity.class, null)
                .page(request);
    }

    public List<ChatSessionEntity> querySession(@Valid QueriesPageRequest request) {
        return JpaQueriesExecutor.of(ChatSessionEntity.class, null)
                .page(request);
    }

    /**
     * 会话异常
     */
    public static class ChatException extends RuntimeException {
        public ChatException(String message) {
            super(message);
        }
    }
}
