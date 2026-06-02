package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 音乐生成使用量事件
 *
 * <p>记录 MiniMax 音乐生成的使用量，用于租户计量和费用统计</p>
 *
 * <h2>计量方式</h2>
 * <ul>
 *   <li>outputDuration - 输出音频时长（毫秒）</li>
 *   <li>outputSize - 输出文件大小（字节）</li>
 *   <li>isInstrumental - 是否纯音乐</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * MusicUsageEvent event = new MusicUsageEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("music-2.6")
 *         .aiType(AiContextMetadata.AiType.MUSIC)
 *         .tenantId("tenant-001")
 *         .sessionId("session-123")
 *         .build(),
 *     MusicUsageData.builder()
 *         .inputChars(150)
 *         .outputDuration(25364)
 *         .outputSize(813651)
 *         .isInstrumental(false)
 *         .audioFormat("mp3")
 *         .build()
 * );
 *
 * eventPublisher.publishEvent(event);
 * }</pre>
 *
 * @see AiUsageEvent
 */
@Getter
public class MusicUsageEvent extends AiUsageEvent<MusicUsageEvent.MusicUsageData> {

    public MusicUsageEvent(AiContextMetadata metadata, MusicUsageData data) {
        super(metadata, data, Instant.now());
    }

    @Override
    public String getEventType() {
        return "MUSIC_USAGE";
    }

    /**
     * 音乐生成使用量数据
     */
    @Getter
    @Builder
    public static class MusicUsageData {
        /**
         * 输入字符数（prompt + lyrics）
         */
        private Integer inputChars;

        /**
         * 输出音频时长（毫秒）
         */
        private Integer outputDuration;

        /**
         * 输出文件大小（字节）
         */
        private Integer outputSize;

        /**
         * 是否纯音乐
         */
        private Boolean isInstrumental;

        /**
         * 音频格式
         */
        private String audioFormat;

        /**
         * 追踪 ID
         */
        private String traceId;
    }
}
