package com.vex.owl.ai.domain.pipeline;

import com.vex.owl.ai.domain.agent.*;
import com.vex.owl.ai.domain.context.ContextAdvisor;
import com.vex.owl.ai.domain.context.DefaultRunContext;
import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.llm.entity.ModelEntity;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SequentialPipeline 集成测试
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class SequentialPipelineIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(SequentialPipelineIntegrationTest.class);

    @Autowired
    ModelProductFactory modelProductFactory;

    @Autowired
    ContextAdvisor contextAdvisor;

    private static ModelEntity.ModelEntityBuilder modelBuilder() {
        return ModelEntity.builder().providerCode("deepseek");
    }

    @Test
    void should_execute_agents_sequentially() {
        log.info("══════ SequentialPipeline 集成测试 ══════");

        // given
        RunContext context = DefaultRunContext.of("test", "test-tenant");

        ChatClient client = modelProductFactory.getFactory("deepseek")
                .createClient(modelBuilder()
                        .modelName("deepseek-chat")
                        .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                        .baseUrl("https://api.deepseek.com")
                        .build());

        // 问候 Agent
        Agent greetingAgent = SimplAgent.builder()
                .client(client)
                .advisorSpecConsumer(spec -> spec.advisors(contextAdvisor))
                .systemSpecConsumer(spec -> spec.text("你是问候助手，简短回复。"))
                .build();

        // 告别 Agent
        Agent farewellAgent = SimplAgent.builder()
                .client(client)
                .advisorSpecConsumer(spec -> spec.advisors(contextAdvisor))
                .systemSpecConsumer(spec -> spec.text("你是告别助手，在回复前加'再见，'。简短回复。"))
                .build();

        AgentManager agentManager = new DefaultAgentManager(List.of(greetingAgent, farewellAgent));

        SequentialPipeline pipeline = SequentialPipeline.builder()
                .client(client)
                .agentManager(agentManager)
                .build();

        // when
        String result = pipeline.execute("跟小明打个招呼然后告别", context);

        // then
        log.info("管道执行结果: {}", result);
        assertNotNull(result);
        assertFalse(result.isBlank());
        log.info("测试通过 ✓");
    }
}
