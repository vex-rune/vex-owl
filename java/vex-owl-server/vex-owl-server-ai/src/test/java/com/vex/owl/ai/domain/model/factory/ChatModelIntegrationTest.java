package com.vex.owl.ai.domain.model.factory;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("integration")
@DisplayName("模型调用集成测试")
class ChatModelIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ChatModelIntegrationTest.class);

    @BeforeAll
    static void checkAnyKeyAvailable() {
        boolean hasDashScope = envSet("DASHSCOPE_API_KEY");
        boolean hasDeepSeek = envSet("DEEPSEEK_API_KEY");
        boolean hasMiniMax = envSet("MINIMAX_API_KEY");
        log.info("══════ 集成测试环境检查 ══════");
        log.info("DASHSCOPE_API_KEY : {}", hasDashScope ? "已设置" : "未设置");
        log.info("DEEPSEEK_API_KEY  : {}", hasDeepSeek ? "已设置" : "未设置");
        log.info("MINIMAX_API_KEY   : {}", hasMiniMax ? "已设置" : "未设置");
        assumeTrue(hasDashScope || hasDeepSeek || hasMiniMax,
                "至少设置一个 API Key 环境变量才能运行集成测试");
    }

    // ═══════════════════════════════════════════
    // DashScope（通义千问）
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("DashScope: 通义千问简单问答应返回非空回复")
    void dashScopeShouldReturnNonEmptyReply() {
        assumeTrue(envSet("DASHSCOPE_API_KEY"), "DASHSCOPE_API_KEY 未设置，跳过");

        log.info("--- DashScope 问答测试开始 ---");
        AiModelEntity model = model("dashscope")
                .modelName("qwen-plus")
                .apiKey(env("DASHSCOPE_API_KEY"))
                .baseUrl("https://dashscope.aliyuncs.com/api/v1")
                .build();
        log.info("构建模型配置: modelName={}, baseUrl={}", model.getModelName(), model.getBaseUrl());

        DashScopeChatModelProviderFactory factory = new DashScopeChatModelProviderFactory();
        factory.restClientBuilder = RestClient.builder();
        ChatClient client = factory.createClient(model);
        log.info("ChatClient 创建成功，发送测试 prompt...");

        String reply = client.prompt("请回复一句话：'测试成功'。不要加其他内容。").call().content();
        log.info("DashScope 回复: {}", reply);

        assertNotNull(reply);
        assertFalse(reply.isBlank());
        log.info("--- DashScope 问答测试通过 ✓ ---");
    }

    // ═══════════════════════════════════════════
    // DeepSeek
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("DeepSeek: 简单问答应返回非空回复")
    void deepSeekShouldReturnNonEmptyReply() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- DeepSeek 问答测试开始 ---");
        AiModelEntity model = model("deepseek")
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();
        log.info("构建模型配置: modelName={}, baseUrl={}", model.getModelName(), model.getBaseUrl());

        DeepSeekChatModelProviderFactory factory = new DeepSeekChatModelProviderFactory();
        factory.restClientBuilder = RestClient.builder();
        ChatClient client = factory.createClient(model);
        log.info("ChatClient 创建成功，发送测试 prompt...");

        String reply = client.prompt("请回复一句话：'测试成功'。不要加其他内容。").call().content();
        log.info("DeepSeek 回复: {}", reply);

        assertNotNull(reply);
        assertFalse(reply.isBlank());
        log.info("--- DeepSeek 问答测试通过 ✓ ---");
    }

    @Test
    @DisplayName("DeepSeek: 代码生成应返回有效代码")
    void deepSeekShouldGenerateValidCode() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- DeepSeek 代码生成测试开始 ---");
        AiModelEntity model = model("deepseek")
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();
        log.info("构建模型配置: modelName={}, baseUrl={}", model.getModelName(), model.getBaseUrl());

        DeepSeekChatModelProviderFactory factory = new DeepSeekChatModelProviderFactory();
        factory.restClientBuilder = RestClient.builder();
        ChatClient client = factory.createClient(model);
        log.info("ChatClient 创建成功，发送代码生成 prompt...");

        String reply = client.prompt("写一个 Java hello world，只输出代码不要解释。").call().content();
        log.info("DeepSeek 代码生成回复:\n{}", reply);

        assertNotNull(reply);
        assertTrue(reply.contains("Hello") || reply.contains("class "),
                "代码生成应包含 Hello 或 class，实际=" + reply);
        log.info("--- DeepSeek 代码生成测试通过 ✓ ---");
    }

    // ═══════════════════════════════════════════
    // MiniMax
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("MiniMax: 简单问答应返回非空回复")
    void miniMaxShouldReturnNonEmptyReply() {
        assumeTrue(envSet("MINIMAX_API_KEY"), "MINIMAX_API_KEY 未设置，跳过");

        log.info("--- MiniMax 问答测试开始 ---");
        AiModelEntity model = model("minimax")
                .modelName("MiniMax-M2.7")
                .apiKey(env("MINIMAX_API_KEY"))
                .baseUrl("https://api.minimax.chat")
                .build();
        log.info("构建模型配置: modelName={}, baseUrl={}", model.getModelName(), model.getBaseUrl());

        MiniMaxChatModelProviderFactory factory = new MiniMaxChatModelProviderFactory();
        ChatClient client = factory.createClient(model);
        log.info("ChatClient 创建成功，发送测试 prompt...");

        String reply = client.prompt("请回复一句话：'测试成功'。不要加其他内容。").call().content();
        log.info("MiniMax 回复: {}", reply);

        assertNotNull(reply);
        assertFalse(reply.isBlank());
        log.info("--- MiniMax 问答测试通过 ✓ ---");
    }

    // ═══════════════════════════════════════════
    // 全链路：AiChatModelProductFactory → 实际调用
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("全链路: AiChatModelProductFactory → DeepSeek → 实际调用")
    void fullChainProductFactoryToDeepSeek() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- 全链路测试开始 ---");
        AiChatModelProductFactory productFactory = new AiChatModelProductFactory();
        log.info("AiChatModelProductFactory 创建成功");

        AiModelEntity model = model("deepseek")
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();
        log.info("构建模型配置: providerCode={}, modelName={}", model.getProviderCode(), model.getModelName());

        AbstractAiChatModelFactory factory = productFactory.get(model.getProviderCode());
        log.info("工厂路由结果: {}", factory.getClass().getSimpleName());

        assertNotNull(factory);
        assertInstanceOf(DeepSeekChatModelProviderFactory.class, factory);

        ((DeepSeekChatModelProviderFactory) factory).restClientBuilder = RestClient.builder();
        ChatClient client = factory.createClient(model);
        log.info("ChatClient 创建成功，发送 prompt...");

        String reply = client.prompt("1+1等于几？只回答数字。").call().content();
        log.info("全链路回复: {}", reply);

        assertNotNull(reply);
        assertTrue(reply.contains("2"));
        log.info("--- 全链路测试通过 ✓ ---");
    }

    // ═══════════════════════════════════════════
    // 工具方法
    // ═══════════════════════════════════════════

    private static boolean envSet(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }

    private static String env(String name) {
        return System.getenv(name);
    }

    private static AiModelEntity.AiModelEntityBuilder model(String providerCode) {
        return AiModelEntity.builder().providerCode(providerCode);
    }
}
