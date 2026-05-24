package com.vex.owl.ai.domain.model.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI路由规则实体
 * <p>对应数据库 ai_routing_rule 表，定义关键词到目标模型的映射规则。
 * KeywordRouter 按优先级逐条匹配用户消息中的关键词，命中后路由到指定模型。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "route")
@Table(name = "ai_routing_rule")
public class AiRoutingRuleEntity {

    /** 主键，带业务前缀的雪花ID */
    @Id
    @BizSnowId
    private String id;

    /** 匹配关键词，多个关键词用逗号分隔 */
    private String keywords;

    /** 命中的目标模型 ID，指向 ai_model 表的记录 */
    private String targetModelId;

    /** 优先级，数字越小优先级越高 */
    private int priority;
}
