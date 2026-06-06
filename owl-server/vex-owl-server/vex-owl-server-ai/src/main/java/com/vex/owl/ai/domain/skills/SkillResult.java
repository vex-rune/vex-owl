package com.vex.owl.ai.domain.skills;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能执行结果
 * <p>统一封装各技能的输出，包含运行时元数据、Token 消耗、模型信息和业务数据。</p>
 *
 * @param <R> 业务数据类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillResult<R> {

    /** 成功状态码 */
    public static final String CODE_SUCCESS = "success";
    /** 错误状态码 */
    public static final String CODE_ERROR = "error";

    /** 状态码 */
    private String code;
    /** 消息类型 */
    private ResultType type;
    /** 运行时元数据 */
    private Metadata metadata;
    /** 业务数据 */
    private R data;

    /** 消息类型枚举 */
    public enum ResultType {
        TEXT,
        FORM,
        TASK,
        FINISH
    }

    /**
     * 运行时元数据
     * <p>贯穿技能执行全程的上下文信息及资源消耗统计。</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        /// 全局唯一任务ID（用于恢复上下文）
        private String taskId;
        /// 会话ID（贯穿全程）
        private String sessionId;
        /// 消息ID（贯穿全程）
        private String messageId;
        /// 调用的技能名称（用于记录）
        private String skillName;
        /// 租户ID（贯穿全程）
        private String tenantId;
        /// Token 消耗
        private TokenUsage tokenUsage;
        /// 使用的模型名称
        private String modelName;
    }

    /**
     * Token 消耗统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        /// 提示词 Token 数
        private int promptTokens;
        /// 生成 Token 数
        private int completionTokens;
        /// 总 Token 数
        private int totalTokens;
    }
}
