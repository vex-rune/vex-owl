package com.vex.owl.ai.domain.chat;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息实体
 * <p>持久化 LLM 对话历史，按会话 ID 和租户分组，支持按时间检索最近 N 条消息。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "cm")
@Table(name = "chat_message")
public class ChatMessageEntity extends JpaBasicWithIdEntity {

    /**
     * 主键
     */
    @Id
    @BizSnowId
    private String id;

    /// 租户ID（多租户隔离）
    private String tenantId;

    /// 会话ID（关联同一轮对话）
    private String conversationId;

    /// 消息类型（USER / ASSISTANT / SYSTEM / TOOL）
    private String messageType;

    /// 消息文本内容
    private String textContent;

}
