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
    public ChatSessionEntity createSession(String userId, String title) {
        ChatSessionEntity session = ChatSessionEntity.builder()
                .userId(userId)
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
    public Page<ChatSessionEntity> getSessions(String userId, int page, int size) {
        return sessionRepository.findByUserIdAndStatusNotOrderByPinnedDescCreateTimeDesc(
                userId, "DELETED", org.springframework.data.domain.PageRequest.of(page, size));
    }

    /**
     * 获取会话详情(如果没有会创建)
     * 根据 租户ID + 会话类型 查询，返回第一个匹配的会话
     */
    @Transactional
    public ChatSessionEntity createSessionByType(String userId, String sessionType) {
        return sessionRepository.findFirstByUserIdAndSessionType(userId, sessionType)
                .orElseGet(() -> {
                    ChatSessionEntity chatSessionEntity = new ChatSessionEntity();
                    chatSessionEntity.setUserId(userId);
                    chatSessionEntity.setSessionType(sessionType);
                    chatSessionEntity.setTitle("新对话");
                    chatSessionEntity.setStatus("ACTIVE");
                    chatSessionEntity.setMessageCount(0);
                    chatSessionEntity.setPinned(false);
                    chatSessionEntity.setStarred(false);
                    entityManager.persist(chatSessionEntity);
                    return chatSessionEntity;
                });
    }

    /**
     * 根据 ID 和租户获取会话
     */
    public Optional<ChatSessionEntity> getSessionById(String sessionId, String userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId);
    }

    /**
     * 更新会话标题
     */
    @Transactional
    public Optional<ChatSessionEntity> updateSessionTitle(String sessionId, String userId, String title) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .map(session -> {
                    session.setTitle(title);
                    return sessionRepository.save(session);
                });
    }

    /**
     * 软删除会话
     */
    @Transactional
    public boolean deleteSession(String sessionId, String userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .map(session -> {
                    session.setStatus("DELETED");
                    sessionRepository.save(session);
                    return true;
                })
                .orElse(false);
    }


    // ==================== 消息管理 ====================

    /**
     * 批量保存消息
     */
    @Transactional
    public List<ChatMessageEntity> saveMessages(List<ChatMessageEntity> messages) {
        return messageRepository.saveAll(messages);
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

    public Optional<ChatSessionEntity> findById(String conversationId) {
        log.debug("findById, conversationId: {}", conversationId);
        return sessionRepository.findById(conversationId);
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
