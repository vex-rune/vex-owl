package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * TTS 语音合成使用量事件
 */
@Getter
@Setter
public class VoiceUsageEvent implements Serializable {

    private String userId;
    private String sessionId;
    private String provider;
    private String modelName;
    private Integer inputChars;
    private Integer outputDuration;
    private Integer outputSize;
    private Integer callCount;
    private String voiceId;
    private String audioFormat;
    private long timestamp;

    public VoiceUsageEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public VoiceUsageEvent(String userId, String sessionId, String provider, String modelName,
                           Integer inputChars, Integer outputDuration, Integer outputSize,
                           Integer callCount, String voiceId, String audioFormat) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.inputChars = inputChars;
        this.outputDuration = outputDuration;
        this.outputSize = outputSize;
        this.callCount = callCount;
        this.voiceId = voiceId;
        this.audioFormat = audioFormat;
        this.timestamp = System.currentTimeMillis();
    }
}