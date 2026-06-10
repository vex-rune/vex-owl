package com.vex.owl.ai.domain.chat;

import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 对话会话实体
 *
 * <p>管理用户的对话会话，支持多租户隔离</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chat_session", indexes = {
    @Index(name = "idx_session_tenant", columnList = "userId"),
    @Index(name = "idx_session_create_time", columnList = "userId,createTime")
})
public class ChatSessionEntity extends JpaBasicEntity {

    @Id
    @BizSnowId( "chat")
    private String id;

    /** 租户ID */
    private String userId;

    /** 会话类型（如：CHAT、AGENT、PIPELINE） */
    private String sessionType;

    /** 会话标题（自动生成或用户设置） */
    private String title;

    /** 会话状态：ACTIVE / ARCHIVED / DELETED */
    private String status;

    /** 最新消息摘要（用于列表展示） */
    private String lastMessage;

    /** 最新消息时间 */
    private LocalDateTime lastMessageTime;

    /** 消息数量 */
    private Integer messageCount;

    /** 是否置顶 */
    private Boolean pinned;

    /** 是否收藏 */
    private Boolean starred;
}
