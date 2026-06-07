package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 音乐生成使用量事件
 */
@Getter
@Setter
public class MusicUsageEvent {

    private String tenantId;
    private String sessionId;
    private String modelName;
    private Integer inputChars;
    private Integer outputDuration;
    private Integer outputSize;
    private Boolean isInstrumental;
    private String audioFormat;
    private String traceId;
    private long timestamp;

    public MusicUsageEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public MusicUsageEvent(String tenantId, String sessionId, String modelName,
                           Integer inputChars, Integer outputDuration, Integer outputSize,
                           Boolean isInstrumental, String audioFormat, String traceId) {
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.outputDuration = outputDuration;
        this.outputSize = outputSize;
        this.isInstrumental = isInstrumental;
        this.audioFormat = audioFormat;
        this.traceId = traceId;
        this.timestamp = System.currentTimeMillis();
    }
}