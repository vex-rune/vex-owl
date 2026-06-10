package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 图像生成使用量事件
 */
@Getter
@Setter
public class ImageUsageEvent implements Serializable {

    private String userId;
    private String sessionId;
    private String provider;
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
    public ImageUsageEvent(String userId, String sessionId, String provider, String modelName,
                           Integer inputChars, Integer requestCount,
                           Integer successCount, Integer failedCount, String aspectRatio) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.requestCount = requestCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.aspectRatio = aspectRatio;
        this.timestamp = System.currentTimeMillis();
    }
}