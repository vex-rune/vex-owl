package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 图像生成使用量事件
 */
@Getter
@Setter
public class ImageUsageEvent {

    private String tenantId;
    private String sessionId;
    private String modelName;
    private Integer inputChars;
    private Integer requestCount;
    private Integer successCount;
    private Integer failedCount;
    private String aspectRatio;
    private long timestamp;

    public ImageUsageEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public ImageUsageEvent(String tenantId, String sessionId, String modelName,
                           Integer inputChars, Integer requestCount,
                           Integer successCount, Integer failedCount, String aspectRatio) {
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.requestCount = requestCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.aspectRatio = aspectRatio;
        this.timestamp = System.currentTimeMillis();
    }
}