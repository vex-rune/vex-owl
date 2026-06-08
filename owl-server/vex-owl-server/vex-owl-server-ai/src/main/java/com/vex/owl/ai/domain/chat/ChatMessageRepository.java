package com.vex.owl.ai.domain.chat;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 对话消息仓储
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, String> {
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.conversationId = :conversationId ORDER BY m.createTime DESC, m.id DESC")
    List<ChatMessageEntity> findByConversationIdOrderByCreateTimeDesc(@Param("conversationId") String conversationId, PageRequest of);

    @Query("SELECT m FROM ChatMessageEntity m WHERE m.conversationId = :conversationId ORDER BY m.createTime ASC, m.id ASC")
    List<ChatMessageEntity> findByConversationIdOrderByCreateTimeAsc(@Param("conversationId") String conversationId);
}
