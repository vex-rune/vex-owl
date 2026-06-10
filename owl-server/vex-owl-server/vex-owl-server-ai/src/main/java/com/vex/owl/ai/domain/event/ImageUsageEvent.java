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
    public ImageUsageEvent(String modelName, Integer inputChars, Integer requestCount,
                           Integer successCount, Integer failedCount, String aspectRatio) {
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.requestCount = requestCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.aspectRatio = aspectRatio;
        this.timestamp = System.currentTimeMillis();
    }
}
