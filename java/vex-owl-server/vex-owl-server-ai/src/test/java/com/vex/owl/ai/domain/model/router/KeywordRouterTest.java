package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.entity.AiRoutingRuleEntity;
import com.vex.owl.ai.domain.model.repo.AiRoutingRuleRepository;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("KeywordRouter单元测试")
@ExtendWith(MockitoExtension.class)
class KeywordRouterTest {

    @Mock
    private AiRoutingRuleRepository ruleRepository;

    private KeywordRouter router;

    @BeforeEach
    void setUp() {
        router = new KeywordRouter(ruleRepository);
    }

    @Test
    @DisplayName("消息包含关键词应路由到对应模型")
    void shouldRouteToTargetModelWhenKeywordMatches() {
        AiModelEntity deepseek = model("model_deepseek", "deepseek-chat");
        RouteContext ctx = new RouteContext("帮我写一个爬虫", null);
        AiRoutingRuleEntity rule = ruleEntity("爬虫,抓取", "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(rule));

        AiModelEntity result = router.route(List.of(deepseek), ctx);

        assertSame(deepseek, result);
    }

    @Test
    @DisplayName("消息包含逗号分隔的第二个关键词也能匹配")
    void shouldMatchWhenSecondKeywordHits() {
        AiModelEntity minimax = model("model_minimax", "minimax-m2.1");
        RouteContext ctx = new RouteContext("请帮我翻译这段英文", null);
        AiRoutingRuleEntity rule = ruleEntity("爬虫,翻译,代码", "model_minimax", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(rule));

        AiModelEntity result = router.route(List.of(minimax), ctx);

        assertSame(minimax, result);
    }

    @Test
    @DisplayName("多条规则按优先级匹配，高优先级规则优先命中")
    void shouldRespectPriorityOrderWhenMultipleRulesMatch() {
        AiModelEntity deepseek = model("model_deepseek", "deepseek-chat");
        AiModelEntity minimax = model("model_minimax", "minimax-m2.1");
        RouteContext ctx = new RouteContext("帮我写爬虫代码", null);

        AiRoutingRuleEntity lowPriority = ruleEntity("代码,编程", "model_minimax", 2);
        AiRoutingRuleEntity highPriority = ruleEntity("爬虫,抓取", "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(highPriority, lowPriority));

        AiModelEntity result = router.route(List.of(deepseek, minimax), ctx);

        assertSame(deepseek, result);
    }

    @Test
    @DisplayName("规则命中但目标模型不在可用列表中应返回null")
    void shouldReturnNullWhenTargetModelNotFound() {
        AiModelEntity other = model("model_other", "other-model");
        RouteContext ctx = new RouteContext("帮我写爬虫", null);
        AiRoutingRuleEntity rule = ruleEntity("爬虫", "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(rule));

        AiModelEntity result = router.route(List.of(other), ctx);

        assertNull(result);
    }

    @Test
    @DisplayName("无任何关键词匹配应返回null")
    void shouldReturnNullWhenNoKeywordMatches() {
        AiModelEntity deepseek = model("model_deepseek", "deepseek-chat");
        RouteContext ctx = new RouteContext("今天天气怎么样", null);
        AiRoutingRuleEntity rule = ruleEntity("爬虫,翻译", "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(rule));

        AiModelEntity result = router.route(List.of(deepseek), ctx);

        assertNull(result);
    }

    @Test
    @DisplayName("消息为null应返回null")
    void shouldReturnNullWhenMessageIsNull() {
        AiModelEntity deepseek = model("model_deepseek", "deepseek-chat");
        RouteContext ctx = new RouteContext(null, null);
        AiRoutingRuleEntity rule = ruleEntity("爬虫", "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(rule));

        AiModelEntity result = router.route(List.of(deepseek), ctx);

        assertNull(result);
    }

    @Test
    @DisplayName("keywords为null的规则应跳过")
    void shouldSkipRuleWhenKeywordsIsNull() {
        AiModelEntity deepseek = model("model_deepseek", "deepseek-chat");
        RouteContext ctx = new RouteContext("帮我写爬虫", null);
        AiRoutingRuleEntity nullKeywordRule = ruleEntity(null, "model_deepseek", 1);

        when(ruleRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of(nullKeywordRule));

        AiModelEntity result = router.route(List.of(deepseek), ctx);

        assertNull(result);
    }

    private AiRoutingRuleEntity ruleEntity(String keywords, String targetModelId, int priority) {
        return AiRoutingRuleEntity.builder()
                .id("rule_" + System.nanoTime())
                .keywords(keywords)
                .targetModelId(targetModelId)
                .priority(priority)
                .build();
    }

    private AiModelEntity model(String id, String modelName) {
        return AiModelEntity.builder()
                .id(id)
                .providerCode("deepseek")
                .modelName(modelName)
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.example.com")
                .costScore(0)
                .priority(5)
                .build();
    }

    private static String env(String name) {
        return System.getenv().getOrDefault(name, "sk-test");
    }
}
