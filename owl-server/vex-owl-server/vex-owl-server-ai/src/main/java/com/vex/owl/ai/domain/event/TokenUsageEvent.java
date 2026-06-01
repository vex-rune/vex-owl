package com.vex.owl.ai.domain.llm.event;

import java.util.Map;

public record TokenUsageEvent(
        Map<String, Object> context,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens,
        String modelName) {
}
