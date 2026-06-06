package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * TTS 语音合成使用量事件
 *
 * <p>记录 MiniMax TTS 语音合成的使用量，用于租户计量和费用统计</p>
 *
 * <h2>计量方式</h2>
 * <ul>
 *   <li>outputDuration - 输出音频时长（毫秒）</li>
 *   <li>outputSize - 输出文件大小（字节）</li>
 *   <li>callCount - 调用次数</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * VoiceUsageEvent event = new VoiceUsageEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("speech-2.8-hd")
 *         .aiType(AiContextMetadata.AiType.VOICE)
 *         .tenantId("tenant-001")
 *         .sessionId("session-123")
 *         .build(),
 *     VoiceUsageData.builder()
 *         .inputChars(25)
 *         .outputDuration(3500)
 *         .outputSize(56000)
 *         .callCount(1)
 *         .voiceId("male-qn-qingse")
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
public class VoiceUsageEvent extends AiUsageEvent<VoiceUsageEvent.VoiceUsageData> {

    public VoiceUsageEvent(AiContextMetadata metadata, VoiceUsageData data) {
        super(metadata, data, Instant.now());
    }

    @Override
    public String getEventType() {
        return "VOICE_USAGE";
    }

    /**
     * TTS 使用量数据
     */
    @Getter
    @Builder
    public static class VoiceUsageData {
        /**
         * 输入字符数
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
         * 调用次数
         */
        private Integer callCount;

        /**
         * 消耗字符数
         */
        private Integer usageChars;

        /**
         * 语音 ID
         */
        private String voiceId;

        /**
         * 音频格式
         */
        private String audioFormat;
    }
}
