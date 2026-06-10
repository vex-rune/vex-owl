package com.vex.owl.ai.domain.usage;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 使用量统计响应 DTO
 */
@Data
@Builder
public class UsageStatResponse {

    /** 租户ID */
    private String userId;

    /** 统计日期 */
    private LocalDate statDate;

    /** 开始日期（范围查询时） */
    private LocalDate startDate;

    /** 结束日期（范围查询时） */
    private LocalDate endDate;

    /** 对话使用量 */
    private ChatUsage chatUsage;

    /** 语音使用量 */
    private VoiceUsage voiceUsage;

    /** 图像使用量 */
    private ImageUsage imageUsage;

    /** 音乐使用量 */
    private MusicUsage musicUsage;

    /** 总调用次数 */
    private Long totalCallCount;

    @Data
    @Builder
    public static class ChatUsage {
        private Long promptTokens;
        private Long completionTokens;
        private Long totalTokens;
        private Long callCount;
    }

    @Data
    @Builder
    public static class VoiceUsage {
        private Long callCount;
        private Long inputChars;
        private Long outputDuration;
        private Long outputDurationSeconds;
        private Long outputSize;
        private Long outputSizeMB;
    }

    @Data
    @Builder
    public static class ImageUsage {
        private Long requestCount;
        private Long successCount;
        private Long failedCount;
        private Long inputChars;
    }

    @Data
    @Builder
    public static class MusicUsage {
        private Long callCount;
        private Long inputChars;
        private Long outputDuration;
        private Long outputDurationSeconds;
        private Long outputSize;
        private Long outputSizeMB;
    }
}
