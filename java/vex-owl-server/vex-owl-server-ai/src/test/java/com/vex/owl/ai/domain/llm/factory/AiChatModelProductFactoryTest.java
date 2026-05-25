package com.vex.owl.ai.domain.llm.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiChatModelProductFactory单元测试")
class AiChatModelProductFactoryTest {

    private AiChatModelProductFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AiChatModelProductFactory();
    }

    @Test
    @DisplayName("传入dashscope应返回DashScopeChatModelProviderFactory实例")
    void shouldReturnDashScopeFactoryWhenProviderCodeIsDashscope() {
        AbstractAiChatModelFactory result = factory.get("dashscope");

        assertNotNull(result);
        assertInstanceOf(DashScopeChatModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入deepseek应返回DeepSeekChatModelProviderFactory实例")
    void shouldReturnDeepSeekFactoryWhenProviderCodeIsDeepseek() {
        AbstractAiChatModelFactory result = factory.get("deepseek");

        assertNotNull(result);
        assertInstanceOf(DeepSeekChatModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入minimax应返回MiniMaxChatModelProviderFactory实例")
    void shouldReturnMiniMaxFactoryWhenProviderCodeIsMinimax() {
        AbstractAiChatModelFactory result = factory.get("minimax");

        assertNotNull(result);
        assertInstanceOf(MiniMaxChatModelProviderFactory.class, result);
    }

    @Test
    @DisplayName("传入未知providerCode应返回null")
    void shouldReturnNullWhenProviderCodeIsUnknown() {
        AbstractAiChatModelFactory result = factory.get("unknown");

        assertNull(result);
    }

    @Test
    @DisplayName("传入null应返回null")
    void shouldReturnNullWhenProviderCodeIsNull() {
        AbstractAiChatModelFactory result = factory.get(null);

        assertNull(result);
    }
}
