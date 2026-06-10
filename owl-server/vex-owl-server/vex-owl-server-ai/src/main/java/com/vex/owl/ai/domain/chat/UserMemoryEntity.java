package com.vex.owl.ai.domain.chat;

import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 用户长期记忆实体
 *
 * <p>存储用户级别的持久化记忆（偏好、事实、上下文等），
 * 按 userId 隔离，支持分类和过期。</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_memory", indexes = {
    @Index(name = "idx_user_memory_tenant", columnList = "userId")
})
public class UserMemoryEntity extends JpaBasicEntity {

    @Id
    @BizSnowId("user_mm")
    private String id;

    /** 租户ID（多租户隔离） */
    private String userId;

    /** 记忆分类：preference / fact / context / summary */
    private String category;

    /** 记忆内容 */
    @Column(length = 10000)
    private String content;

    /** 权重/重要性（0-100），越高越优先展示 */
    private Integer weight;

    /** 是否有效 */
    private Boolean active;
}
