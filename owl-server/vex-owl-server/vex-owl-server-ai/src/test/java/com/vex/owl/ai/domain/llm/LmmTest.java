package com.vex.owl.ai.domain.llm;

import com.vex.owl.ai.app.tools.DateTimeTools;
import com.vex.owl.ai.app.tools.ThinkRecordLogTools;
import com.vex.owl.ai.domain.llm.entity.ModelEntity;
import com.vex.owl.ai.domain.llm.event.TokenUsageEvent;
import com.vex.owl.ai.domain.llm.factory.AbstractAiModelFactory;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("integration")
@DisplayName("ChatClient 集成测试 — 验证 TokenUsageEvent 发送")
@SpringBootTest
class LmmTest {

    private static final Logger log = LoggerFactory.getLogger(LmmTest.class);

    @Autowired
    ModelProductFactory modelProductFactory;

    @Autowired
    TokenUsageEventListener eventListener;

    @Autowired
    ToolCallAdvisor toolCallAdvisor;

    @BeforeAll
    static void checkEnv() {
        boolean hasKey = envSet("DEEPSEEK_API_KEY");
        log.info("══════ LmmTest 集成测试环境检查 ══════");
        log.info("DEEPSEEK_API_KEY : {}", hasKey ? "已设置" : "未设置");
        assumeTrue(hasKey, "需要设置 DEEPSEEK_API_KEY 环境变量才能运行集成测试");
    }

    @Test
    @DisplayName("正常调用应触发 TokenUsageEvent")
    void normalCallShouldTriggerTokenUsageEvent() {
        log.info("--- TokenUsageEvent 发送验证测试开始 ---");

        eventListener.reset();

        AbstractAiModelFactory factory = modelProductFactory.getFactory("deepseek");
        ModelEntity model = modelBuilder()
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .build();
        ChatClient client = factory.createClient(model);

        ToolCallback[] callbacks = ToolCallbacks.from(new ThinkRecordLogTools(), new DateTimeTools());

        ChatClientResponse response = client.prompt("用一句话介绍唐朝")
                .toolCallbacks(callbacks)
                .toolContext(Map.of("A", 1))
                .system("你是一个历史知识问答助手，请简洁回答。")
                .advisors(toolCallAdvisor)
                .call()
                .chatClientResponse();

        assertNotNull(response, "响应不应为空");
        log.info("LLM 响应: {}", response.chatResponse().getResult().getOutput().getText());
        log.info("Token 使用: {}", response.chatResponse().getMetadata().getUsage());

        assertNotNull(eventListener.events, "事件监听器不应为空");
        assertFalse(eventListener.events.isEmpty(), "应捕获到 TokenUsageEvent");

        if (!eventListener.events.isEmpty()) {
            TokenUsageEvent event = eventListener.events.get(0);
            log.info("捕获到 TokenUsageEvent: promptTokens={}, completionTokens={}, totalTokens={}, model={}",
                    event.promptTokens(), event.completionTokens(), event.totalTokens(), event.modelName());
            assertNotNull(event.promptTokens(), "promptTokens 不应为空");
            assertNotNull(event.totalTokens(), "totalTokens 不应为空");
        }

        log.info("--- TokenUsageEvent 发送验证测试通过 ✓ ---");
    }

    @Test
    @DisplayName("流式调用应触发 TokenUsageEvent")
    void streamCallShouldTriggerTokenUsageEvent() {
        log.info("--- 流式 TokenUsageEvent 发送验证测试开始 ---");

        eventListener.reset();

        AbstractAiModelFactory factory = modelProductFactory.getFactory("deepseek");
        ModelEntity model = modelBuilder()
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .build();
        ChatClient client = factory.createClient(model);

        StringBuilder contentBuilder = new StringBuilder();

        ChatClientResponse lastResponse = client.prompt("用三句话介绍宋朝")
                .system("你是一个历史知识问答助手，请简洁回答。")
                .stream()
                .chatClientResponse()
                .doOnNext(response -> {
                    String content = response.chatResponse().getResult().getOutput().getText();
                    if (content != null && !content.isEmpty()) {
                        contentBuilder.append(content);
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.print(content);
                    }
                })
                .doOnComplete(() -> log.info("流式响应完成"))
                .doOnError(error -> log.error("流式响应错误: {}", error.getMessage()))
                .blockLast();

        assertNotNull(lastResponse, "最后一个响应不应为空");
        log.info("完整内容: {}", contentBuilder);
        log.info("Token 使用: {}", lastResponse.chatResponse().getMetadata().getUsage());

        assertNotNull(eventListener.events, "事件监听器不应为空");
        assertFalse(eventListener.events.isEmpty(), "流式调用应捕获到 TokenUsageEvent");

        if (!eventListener.events.isEmpty()) {
            TokenUsageEvent event = eventListener.events.get(0);
            log.info("捕获到流式 TokenUsageEvent: promptTokens={}, completionTokens={}, totalTokens={}, model={}",
                    event.promptTokens(), event.completionTokens(), event.totalTokens(), event.modelName());
            assertNotNull(event.promptTokens(), "promptTokens 不应为空");
            assertNotNull(event.totalTokens(), "totalTokens 不应为空");
        }

        log.info("--- 流式 TokenUsageEvent 发送验证测试通过 ✓ ---");
    }

    @TestConfiguration
    static class TokenUsageEventListener {
        final List<TokenUsageEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onTokenUsageEvent(TokenUsageEvent event) {
            log.info("监听到 TokenUsageEvent: {}", event);
            events.add(event);
        }

        public void reset() {
            events.clear();
        }
    }

    private ModelEntity.ModelEntityBuilder modelBuilder() {
        return ModelEntity.builder()
                .providerCode("deepseek");
    }

    private static String env(String key) {
        return System.getenv(key);
    }

    private static boolean envSet(String key) {
        String value = System.getenv(key);
        return value != null && !value.isEmpty();
    }
}
