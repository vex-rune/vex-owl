package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * 图像生成使用量事件
 *
 * <p>记录 MiniMax 文生图的使用量，用于租户计量和费用统计</p>
 *
 * <h2>计量方式</h2>
 * <ul>
 *   <li>requestCount - 请求数量</li>
 *   <li>successCount - 成功数量</li>
 *   <li>failedCount - 失败数量</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * ImageUsageEvent event = new ImageUsageEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("image-01")
 *         .aiType(AiContextMetadata.AiType.IMAGE)
 *         .tenantId("tenant-001")
 *         .sessionId("session-123")
 *         .build(),
 *     ImageUsageData.builder()
 *         .inputChars(120)
 *         .requestCount(3)
 *         .successCount(3)
 *         .failedCount(0)
 *         .aspectRatio("16:9")
 *         .build()
 * );
 *
 * eventPublisher.publishEvent(event);
 * }</pre>
 *
 * @see AiUsageEvent
 */
@Getter
public class ImageUsageEvent extends AiUsageEvent<ImageUsageEvent.ImageUsageData> {

    public ImageUsageEvent(AiContextMetadata metadata, ImageUsageData data) {
        super(metadata, data, Instant.now());
    }

    @Override
    public String getEventType() {
        return "IMAGE_USAGE";
    }

    /**
     * 图像生成使用量数据
     */
    @Getter
    @Builder
    public static class ImageUsageData {
        /**
         * 输入提示词字符数
         */
        private Integer inputChars;

        /**
         * 请求数量
         */
        private Integer requestCount;

        /**
         * 成功数量
         */
        private Integer successCount;

        /**
         * 失败数量
         */
        private Integer failedCount;

        /**
         * 宽高比
         */
        private String aspectRatio;

        /**
         * 响应格式（url / base64）
         */
        private String responseFormat;

        /**
         * 任务 ID
         */
        private String taskId;
    }
}
