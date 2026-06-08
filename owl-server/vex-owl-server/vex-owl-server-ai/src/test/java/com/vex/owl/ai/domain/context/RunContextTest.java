package com.vex.owl.ai.domain.context;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RunContextTest {

    private ModelProperties sampleProperties() {
        return new ModelProperties() {
            @Override public String getApiKey() { return "test-key"; }
            @Override public String getModelName() { return "deepseek-chat"; }
            @Override public String getBaseUrl() { return "https://api.deepseek.com"; }
            @Override public String getProviderCode() { return "deepseek"; }
        };
    }

    @Test
    void builder_shouldCreateInstance() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .tenantId("tenant-1")
                .sessionId("session-1")
                .build();

        assertThat(ctx.getTenantId()).isEqualTo("tenant-1");
        assertThat(ctx.getSessionId()).isEqualTo("session-1");
        assertThat(ctx.getStep()).isEqualTo(1);
        assertThat(ctx.getId()).isNotBlank();
    }

    @Test
    void builder_shouldHandleNullHeadersAndParams() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();

        assertThat(ctx.getHeaders()).isNotNull().isEmpty();
        assertThat(ctx.getParams()).isNotNull().isEmpty();
    }

    @Test
    void toMap_shouldContainAllFields() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .tenantId("tenant-1")
                .sessionId("session-1")
                .headers(Map.of("h1", "v1"))
                .params(Map.of("p1", "v1"))
                .build();

        Map<String, Object> map = ctx.toMap();

        assertThat(map).containsEntry("tenantId", "tenant-1");
        assertThat(map).containsEntry("sessionId", "session-1");
        assertThat(map).containsEntry("step", 1);
        assertThat(map).containsKey("id");
        assertThat(map).containsKey("startTime");
        assertThat(map).containsEntry("headers", Map.of("h1", "v1"));
        assertThat(map).containsEntry("params", Map.of("p1", "v1"));
    }

    @Test
    void addStep_shouldIncrementStep() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();

        assertThat(ctx.getStep()).isEqualTo(1);

        ctx.addStep();
        assertThat(ctx.getStep()).isEqualTo(2);

        ctx.addStep();
        assertThat(ctx.getStep()).isEqualTo(3);
    }

    @Test
    void addStep_shouldReturnSelf() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();

        RunContext returned = ctx.addStep();
        assertThat(returned).isSameAs(ctx);
    }

    @Test
    void withResult_shouldSetPreviousResult() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();

        assertThat(ctx.getPreviousResult()).isNull();

        ctx.withResult("step-1-output");
        assertThat(ctx.getPreviousResult()).isEqualTo("step-1-output");
    }

    @Test
    void withResult_shouldReturnSelf() {
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();

        RunContext returned = ctx.withResult("result");
        assertThat(returned).isSameAs(ctx);
    }

    @Test
    void startTime_shouldBeRecent() {
        long before = System.currentTimeMillis();
        RunContext ctx = RunContext.builder()
                .modelProperties(sampleProperties())
                .build();
        long after = System.currentTimeMillis();

        assertThat(ctx.getStartTime()).isBetween(before, after);
    }
}
