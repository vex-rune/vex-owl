package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PrimaryRouter单元测试")
class PrimaryRouterTest {

    private PrimaryRouter router;
    private RouteContext ctx;

    @BeforeEach
    void setUp() {
        router = new PrimaryRouter();
        ctx = new RouteContext("用户消息", null);
    }

    @Test
    @DisplayName("存在主AI时直接返回主AI模型")
    void shouldReturnPrimaryModelWhenPrimaryExists() {
        AiModelEntity primary = model().isPrimary(true).modelName("主AI").build();
        AiModelEntity other = model().modelName("其他模型").build();

        AiModelEntity result = router.route(List.of(other, primary), ctx);

        assertSame(primary, result);
    }

    @Test
    @DisplayName("无主AI时回退到默认模型")
    void shouldFallbackToDefaultModelWhenNoPrimary() {
        AiModelEntity defaultModel = model().isDefault(true).modelName("默认模型").build();
        AiModelEntity other = model().modelName("其他模型").build();

        AiModelEntity result = router.route(List.of(other, defaultModel), ctx);

        assertSame(defaultModel, result);
    }

    @Test
    @DisplayName("无主AI也无默认模型时回退到最高优先级模型")
    void shouldFallbackToHighestPriorityWhenNoPrimaryAndNoDefault() {
        AiModelEntity lowPriority = model().priority(10).modelName("低优先级").build();
        AiModelEntity highPriority = model().priority(1).modelName("高优先级").build();
        AiModelEntity midPriority = model().priority(5).modelName("中优先级").build();

        AiModelEntity result = router.route(List.of(lowPriority, highPriority, midPriority), ctx);

        assertSame(highPriority, result);
    }

    @Test
    @DisplayName("空模型列表应返回null")
    void shouldReturnNullWhenModelListIsEmpty() {
        AiModelEntity result = router.route(List.of(), ctx);

        assertNull(result);
    }

    private AiModelEntity.AiModelEntityBuilder model() {
        return AiModelEntity.builder()
                .id("id_" + System.nanoTime())
                .providerCode("deepseek")
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.example.com")
                .isPrimary(false)
                .isDefault(false)
                .priority(5)
                .costScore(0);
    }

    private static String env(String name) {
        return System.getenv().getOrDefault(name, "sk-test");
    }
}
