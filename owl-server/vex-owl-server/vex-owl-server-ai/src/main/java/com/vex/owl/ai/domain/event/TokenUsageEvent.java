package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Token 使用量事件
 */
@Getter
@Setter
@ToString
public class TokenUsageEvent implements Serializable {

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
    public TokenUsageEvent(String provider, String modelName, Integer promptTokens,
                          Integer completionTokens, Integer totalTokens) {
        this.provider = provider;
        this.modelName = modelName;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
        this.timestamp = System.currentTimeMillis();
    }
}
