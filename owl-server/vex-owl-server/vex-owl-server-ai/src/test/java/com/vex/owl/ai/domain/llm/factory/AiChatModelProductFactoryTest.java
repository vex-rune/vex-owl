package com.vex.owl.ai.domain.llm.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiChatModelProductFactory单元测试")
class AiChatModelProductFactoryTest {

    private ModelProductFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ModelProductFactory();
    }

    @Test
    @DisplayName("传入dashscope应返回DashScopeChatModelProviderFactory实例")
    void shouldReturnDashScopeFactoryWhenProviderCodeIsDashscope() {
        AbstractAiModelFactory result = factory.getFactory("dashscope");

        assertNotNull(result);
        assertInstanceOf(DashScopeModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入deepseek应返回DeepSeekChatModelProviderFactory实例")
    void shouldReturnDeepSeekFactoryWhenProviderCodeIsDeepseek() {
        AbstractAiModelFactory result = factory.getFactory("deepseek");

        assertNotNull(result);
        assertInstanceOf(DeepSeekModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入minimax应返回MiniMaxChatModelProviderFactory实例")
    void shouldReturnMiniMaxFactoryWhenProviderCodeIsMinimax() {
        AbstractAiModelFactory result = factory.getFactory("minimax");

        assertNotNull(result);
        assertInstanceOf(MiniMaxModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入未知providerCode应返回null")
    void shouldReturnNullWhenProviderCodeIsUnknown() {
        AbstractAiModelFactory result = factory.getFactory("unknown");

        assertNull(result);
    }

    @Test
    @DisplayName("传入null应返回null")
    void shouldReturnNullWhenProviderCodeIsNull() {
        AbstractAiModelFactory result = factory.getFactory(null);

        assertNull(result);
    }
}
