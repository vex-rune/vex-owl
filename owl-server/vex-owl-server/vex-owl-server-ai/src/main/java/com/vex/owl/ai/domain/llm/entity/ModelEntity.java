package com.vex.owl.ai.domain.llm.entity;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * AI模型实体
 * <p>对应数据库 ai_model 表，承载模型的全部元信息——
 * 既包含 Provider 连接参数（供 Factory 层消费），
 * 也包含路由策略信息（供 Router 层消费）。</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "model")
@Table(name = "ai_model")
public class ModelEntity implements ModelProperties {

    /** 主键，带业务前缀的雪花ID */
    @Id
    @BizSnowId
    private String id;

    /** 租户ID，用于多租户数据隔离 */
    private String tenantId;

    /** Provider 代码，如 "dashscope"、"deepseek"、"minimax"，用于工厂路由 */
    private String providerCode;

    /** 模型名称，如 "qwen-plus"、"deepseek-chat" */
    private String modelName;

    /** API 密钥，对接模型提供商的身份凭证*/
    private String apiKey;

    /** API 基础地址，模型提供商的请求入口 URL , 如果为空则是官方指定的url */
    private String baseUrl;

    /** 是否为主 AI，同一时间全局只有一个为 true */
    private boolean isPrimary;

    /** 是否为默认回退模型 */
    private boolean isDefault;

    /** 优先级，数字越小优先级越高，用于故障转移 */
    private int priority;

    /** 故障转移目标模型 ID，当前模型不可用时切换到该模型 */
    private String fallbackModelId;

    /** 成本评分，用于成本优先策略 */
    private double costScore;

    /** 扩展参数，承载模型特有的额外配置项 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> options;
}
