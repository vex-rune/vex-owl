package com.vex.owl.ai.domain.chat;

import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话消息仓储
 */
@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessageEntity, String> {
    List<AiChatMessageEntity> findByConversationIdOrderByCreateTimeDesc(String conversationId, PageRequest of);
}
