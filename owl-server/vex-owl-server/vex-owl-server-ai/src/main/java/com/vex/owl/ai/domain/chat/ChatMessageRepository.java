package com.vex.owl.ai.domain.chat;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话消息仓储
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {
    List<ChatMessageEntity> findByConversationIdOrderByCreateTimeDesc(String conversationId, PageRequest of);

    List<ChatMessageEntity> findByConversationIdOrderByCreateTimeAsc(String conversationId);
}
