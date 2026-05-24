package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CostRouter单元测试")
class CostRouterTest {

    private CostRouter router;
    private RouteContext ctx;

    @BeforeEach
    void setUp() {
        router = new CostRouter();
        ctx = new RouteContext("用户消息", null);
    }

    @Test
    @DisplayName("多个模型有不同costScore时应返回最小的")
    void shouldReturnModelWithLowestCostScore() {
        AiModelEntity expensive = model().costScore(0.05).modelName("昂贵模型").build();
        AiModelEntity cheap = model().costScore(0.001).modelName("便宜模型").build();
        AiModelEntity medium = model().costScore(0.01).modelName("中等模型").build();

        AiModelEntity result = router.route(List.of(expensive, cheap, medium), ctx);

        assertSame(cheap, result);
    }

    @Test
    @DisplayName("costScore为0的模型应被过滤不参与比较")
    void shouldFilterOutModelsWithZeroCostScore() {
        AiModelEntity zeroCost = model().costScore(0).modelName("零成本模型").build();
        AiModelEntity validCost = model().costScore(0.01).modelName("有效成本模型").build();

        AiModelEntity result = router.route(List.of(zeroCost, validCost), ctx);

        assertSame(validCost, result);
    }

    @Test
    @DisplayName("所有costScore都为0时应返回null")
    void shouldReturnNullWhenAllCostScoresAreZero() {
        AiModelEntity zero1 = model().costScore(0).modelName("零成本1").build();
        AiModelEntity zero2 = model().costScore(0).modelName("零成本2").build();

        AiModelEntity result = router.route(List.of(zero1, zero2), ctx);

        assertNull(result);
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
                .priority(5);
    }

    private static String env(String name) {
        return System.getenv().getOrDefault(name, "sk-test");
    }
}
