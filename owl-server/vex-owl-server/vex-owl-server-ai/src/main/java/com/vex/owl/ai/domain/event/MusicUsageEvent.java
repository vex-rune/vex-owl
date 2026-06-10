package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 音乐生成使用量事件
 */
@Getter
@Setter
public class MusicUsageEvent implements Serializable {

    private String userId;
    private String sessionId;
    private String provider;
    private String modelName;
    private Integer inputChars;
    private Integer outputDuration;
    private Integer outputSize;
    private Boolean isInstrumental;
    private String audioFormat;
    private long timestamp;

    public MusicUsageEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public MusicUsageEvent(String userId, String sessionId, String provider, String modelName,
                           Integer inputChars, Integer outputDuration, Integer outputSize,
                           Boolean isInstrumental, String audioFormat) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.outputDuration = outputDuration;
        this.outputSize = outputSize;
        this.isInstrumental = isInstrumental;
        this.audioFormat = audioFormat;
        this.timestamp = System.currentTimeMillis();
    }
}