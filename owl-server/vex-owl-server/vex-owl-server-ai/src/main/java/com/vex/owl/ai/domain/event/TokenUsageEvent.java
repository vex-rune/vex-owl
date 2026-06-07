package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Token 使用量事件
 */
@Getter
@Setter
public class TokenUsageEvent {

    private String tenantId;
    private String sessionId;
    private String provider;
    private String modelName;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private long timestamp;

    public TokenUsageEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    @Builder
    public TokenUsageEvent(String tenantId, String sessionId, String provider, 
                          String modelName, Integer promptTokens,
                          Integer completionTokens, Integer totalTokens) {
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.timestamp = System.currentTimeMillis();
    }
}