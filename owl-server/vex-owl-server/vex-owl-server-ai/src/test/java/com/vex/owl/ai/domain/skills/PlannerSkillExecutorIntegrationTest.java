package com.vex.owl.ai.domain.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.vex.owl.ai.app.tools.ThinkRecordLogTools;
import com.vex.owl.ai.domain.llm.entity.ModelEntity;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import com.vex.owl.ai.domain.llm.factory.DeepSeekModelProviderFactory;
import com.vex.owl.ai.domain.skills.SkillResult.Metadata;
import com.vex.owl.ai.domain.skills.SkillResult.ResultType;
import com.vex.owl.ai.domain.skills.plan.Plan;
import com.vex.owl.ai.domain.skills.plan.PlannerSkillExecutor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("integration")
@DisplayName("PlannerSkillExecutor 集成测试 — 调用大模型验证计划生成")
@SpringBootTest
class PlannerSkillExecutorIntegrationTest {

    @Autowired
    ModelProductFactory productFactory;

    private static final Logger log = LoggerFactory.getLogger(PlannerSkillExecutorIntegrationTest.class);

    @BeforeAll
    static void checkAnyKeyAvailable() {
        boolean hasDeepSeek = envSet("DEEPSEEK_API_KEY");
        log.info("══════ PlannerSkillExecutor 集成测试环境检查 ══════");
        log.info("DEEPSEEK_API_KEY : {}", hasDeepSeek ? "已设置" : "未设置");
        assumeTrue(hasDeepSeek, "需要设置 DEEPSEEK_API_KEY 环境变量才能运行集成测试");
    }

    @Test
    @DisplayName("全链路: DeepSeek → PlannerSkillExecutor → 结构化 Plan 输出")
    void fullChainShouldReturnStructuredPlan() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- PlannerSkillExecutor 全链路集成测试开始 ---");

        ModelEntity model = modelBuilder()
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();
        log.info("构建模型: modelName={}", model.getModelName());

        DeepSeekModelProviderFactory factory = new DeepSeekModelProviderFactory();
        ChatClient chatClient = factory.createClient(model);

        Map<String, Object> toolContext = Map.of(
                "tenantId", "test-tenant",
                "sessionId", "test-session",
                "messageId", "test-msg-001"
        );

        PlannerSkillExecutor executor = new PlannerSkillExecutor(
                chatClient,
                toolContext,
                Collections.emptyList(),
                null
        );

        String userMessage = "我想给项目添加一个用户登录功能";

        SkillResult<Plan> result = executor.execute(userMessage);

        assertNotNull(result);
        assertEquals(SkillResult.CODE_SUCCESS, result.getCode());
        assertEquals(ResultType.TASK, result.getType());

        Metadata metadata = result.getMetadata();
        assertNotNull(metadata);
        log.info("metadata: skillName={}, tenantId={}, sessionId={}, modelName={}, tokenUsage={}",
                metadata.getSkillName(), metadata.getTenantId(), metadata.getSessionId(),
                metadata.getModelName(), metadata.getTokenUsage());
        assertEquals(PlannerSkillExecutor.NAME, metadata.getSkillName());
        assertEquals("test-tenant", metadata.getTenantId());
        assertEquals("test-session", metadata.getSessionId());

        Plan plan = result.getData();
        assertNotNull(plan, "Plan 不应为空");
        log.info("Plan title: {}", plan.getTitle());
        log.info("Plan scope: {}", plan.getScope());
        log.info("Plan actionItems 数量: {}", plan.getActionItems() != null ? plan.getActionItems().size() : 0);
        log.info("Plan openQuestions: {}", plan.getOpenQuestions());

        log.info("--- PlannerSkillExecutor 全链路集成测试通过 ✓ ---");
    }

    @Test
    @DisplayName("全链路: DeepSeek → 全量工厂 + PlannerSkillExecutor")
    void fullChainWithProductFactory() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- PlannerSkillExecutor 全量工厂全链路测试开始 ---");

        ModelEntity model = modelBuilder()
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();

        DeepSeekModelProviderFactory factory =
                (DeepSeekModelProviderFactory) productFactory.getFactory(model.getProviderCode());
        assertNotNull(factory);

        ChatClient chatClient = factory.createClient(model);

        Map<String, Object> toolContext = Map.of(
                "tenantId", "tenant-002",
                "sessionId", "sess-002",
                "messageId", "msg-002"
        );

        List<ToolCallback> tools = new ArrayList<>();
        tools.addAll(List.of(ToolCallbacks.from(new ThinkRecordLogTools())));

        PlannerSkillExecutor executor = new PlannerSkillExecutor(
                chatClient,
                toolContext,
                tools,
                null
        );

        SkillResult<Plan> result = executor.execute("帮我设计一个多租户系统的数据隔离方案");

        log.info("code={}, metadata={}", result.getCode(), result.getMetadata());
        assertNotNull(result);
        assertEquals(SkillResult.CODE_SUCCESS, result.getCode());
        assertNotNull(result.getMetadata());
        assertEquals("tenant-002", result.getMetadata().getTenantId());

        Plan plan = result.getData();
        assertNotNull(plan);
        log.info("Plan : {}", plan);

        log.info("--- PlannerSkillExecutor 全量工厂全链路测试通过 ✓ ---");
    }

    @Test
    @DisplayName("简单任务: PlannerSkillExecutor 应快速返回短小计划")
    void simpleTaskShouldReturnQuickly() {
        assumeTrue(envSet("DEEPSEEK_API_KEY"), "DEEPSEEK_API_KEY 未设置，跳过");

        log.info("--- PlannerSkillExecutor 简单任务测试开始 ---");

        ModelEntity model = modelBuilder()
                .modelName("deepseek-chat")
                .apiKey(env("DEEPSEEK_API_KEY"))
                .baseUrl("https://api.deepseek.com")
                .build();

        DeepSeekModelProviderFactory factory = new DeepSeekModelProviderFactory();
        ChatClient chatClient = factory.createClient(model);

        Map<String, Object> toolContext = Map.of(
                "tenantId", "tenant-003",
                "sessionId", "sess-003",
                "messageId", "msg-003"
        );

        PlannerSkillExecutor executor = new PlannerSkillExecutor(
                chatClient,
                toolContext,
                Collections.emptyList(),
                null
        );

        String userMessage = "给 UserApi 的 getUser 方法添加参数校验";

        long start = System.currentTimeMillis();
        SkillResult<Plan> result = executor.execute(userMessage);
        long cost = System.currentTimeMillis() - start;

        log.info("简单任务耗时: {}ms", cost);
        log.info("Plan : {}", result.getData());

        assertNotNull(result);
        assertEquals(SkillResult.CODE_SUCCESS, result.getCode());
        assertTrue(cost < 60000, "简单任务应在 60s 内完成");

        log.info("--- PlannerSkillExecutor 简单任务测试通过 ✓ ---");
    }

    private static boolean envSet(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank();
    }

    private static String env(String name) {
        return System.getenv(name);
    }

    private static ModelEntity.ModelEntityBuilder modelBuilder() {
        return ModelEntity.builder().providerCode("deepseek");
    }
}
