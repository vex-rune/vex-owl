package com.vex.owl.ai.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 对话会话 Repository
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSessionEntity, String> {

    /**
     * 查询租户的会话列表（排除已删除）
     */
    Page<ChatSessionEntity> findByUserIdAndStatusNotOrderByPinnedDescCreateTimeDesc(
            String userId, String status, Pageable pageable);

    /**
     * 根据 ID 和租户查询会话
     */
    Optional<ChatSessionEntity> findByIdAndUserId(String id, String userId);

    /**
     * 根据租户和会话类型查询第一个会话
     */
    Optional<ChatSessionEntity> findFirstByUserIdAndSessionType(String userId, String sessionType);

    /**
     * 统计租户会话数量
     */
    long countByUserIdAndStatus(String userId, String status);
}
