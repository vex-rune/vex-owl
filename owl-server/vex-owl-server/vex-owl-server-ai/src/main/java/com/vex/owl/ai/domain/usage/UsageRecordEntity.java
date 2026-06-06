package com.vex.owl.ai.domain.usage;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 使用量统计实体
 *
 * <p>用于记录和统计各租户的各种 AI 能力消耗情况</p>
 *
 * <h2>统计类型</h2>
 * <ul>
 *   <li>CHAT - 对话 Token 消耗</li>
 *   <li>VOICE - TTS 语音合成（按输出时长和文件大小统计）</li>
 *   <li>IMAGE - 图像生成（按请求数量统计）</li>
 *   <li>MUSIC - 音乐生成（按输出时长和文件大小统计）</li>
 * </ul>
 *
 * <h2>聚合方式</h2>
 * <p>按租户 + 日期 + AI 类型聚合统计，支持日、周、月报表</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "usage")
@Table(name = "ai_usage_record", indexes = {
    @Index(name = "idx_usage_tenant_date", columnList = "tenantId, statDate"),
    @Index(name = "idx_usage_type_date", columnList = "usageType, statDate")
})
public class UsageRecordEntity {

    /** 主键 */
    @Id
    @BizSnowId
    private String id;

    /** 租户ID */
    private String tenantId;

    /** 统计日期 */
    private LocalDate statDate;

    /** AI 类型：CHAT / VOICE / IMAGE / MUSIC */
    private String usageType;

    /** AI 平台：minimax / openai / deepseek 等 */
    private String aiPlatform;

    /** 模型名称 */
    private String modelName;

    // ==================== CHAT 对话统计 ====================

    /** 输入 Token 数（仅 CHAT 类型） */
    private Long promptTokens;

    /** 输出 Token 数（仅 CHAT 类型） */
    private Long completionTokens;

    /** 总 Token 数（仅 CHAT 类型） */
    private Long totalTokens;

    /** 对话请求次数 */
    private Long chatCallCount;

    // ==================== VOICE 语音统计 ====================

    /** 输入字符数（VOICE/MUSIC 类型） */
    private Long inputChars;

    /** 输出时长（毫秒）（VOICE/MUSIC 类型） */
    private Long outputDuration;

    /** 输出大小（字节）（VOICE/MUSIC 类型） */
    private Long outputSize;

    /** 语音合成调用次数（仅 VOICE 类型） */
    private Long voiceCallCount;

    // ==================== IMAGE 图像统计 ====================

    /** 请求数量（仅 IMAGE 类型） */
    private Long imageRequestCount;

    /** 成功数量（仅 IMAGE 类型） */
    private Long imageSuccessCount;

    /** 失败数量（仅 IMAGE 类型） */
    private Long imageFailedCount;

    // ==================== MUSIC 音乐统计 ====================

    /** 音乐生成调用次数（仅 MUSIC 类型） */
    private Long musicCallCount;

    // ==================== 公共字段 ====================

    /** 记录创建时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

    /**
     * 增加 CHAT 统计
     */
    public void addChatUsage(Long promptTokens, Long completionTokens, Long totalTokens) {
        this.promptTokens = (this.promptTokens == null ? 0 : this.promptTokens) + promptTokens;
        this.completionTokens = (this.completionTokens == null ? 0 : this.completionTokens) + completionTokens;
        this.totalTokens = (this.totalTokens == null ? 0 : this.totalTokens) + totalTokens;
        this.chatCallCount = (this.chatCallCount == null ? 0 : this.chatCallCount) + 1;
    }

    /**
     * 增加 VOICE 统计
     */
    public void addVoiceUsage(Long inputChars, Long outputDuration, Long outputSize) {
        this.inputChars = (this.inputChars == null ? 0 : this.inputChars) + inputChars;
        this.outputDuration = (this.outputDuration == null ? 0 : this.outputDuration) + outputDuration;
        this.outputSize = (this.outputSize == null ? 0 : this.outputSize) + outputSize;
        this.voiceCallCount = (this.voiceCallCount == null ? 0 : this.voiceCallCount) + 1;
    }

    /**
     * 增加 IMAGE 统计
     */
    public void addImageUsage(Long inputChars, Long requestCount, Long successCount, Long failedCount) {
        this.inputChars = (this.inputChars == null ? 0 : this.inputChars) + inputChars;
        this.imageRequestCount = (this.imageRequestCount == null ? 0 : this.imageRequestCount) + requestCount;
        this.imageSuccessCount = (this.imageSuccessCount == null ? 0 : this.imageSuccessCount) + successCount;
        this.imageFailedCount = (this.imageFailedCount == null ? 0 : this.imageFailedCount) + failedCount;
    }

    /**
     * 增加 MUSIC 统计
     */
    public void addMusicUsage(Long inputChars, Long outputDuration, Long outputSize) {
        this.inputChars = (this.inputChars == null ? 0 : this.inputChars) + inputChars;
        this.outputDuration = (this.outputDuration == null ? 0 : this.outputDuration) + outputDuration;
        this.outputSize = (this.outputSize == null ? 0 : this.outputSize) + outputSize;
        this.musicCallCount = (this.musicCallCount == null ? 0 : this.musicCallCount) + 1;
    }

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
