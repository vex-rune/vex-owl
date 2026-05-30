package com.vex.owl.ai.domain.llm;

import com.vex.owl.ai.app.tools.DateTimeTools;
import com.vex.owl.ai.app.tools.ThinkRecordLogTools;
import com.vex.owl.ai.domain.llm.entity.ModelEntity;
import com.vex.owl.ai.domain.llm.factory.AbstractAiModelFactory;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@Tag("integration")
@Slf4j
@SpringBootTest
class LmmTest {

    @Autowired
    ModelProductFactory modelProductFactory;

    @Resource(name = "FilterToolCallAdvisor")
    ToolCallAdvisor toolCallAdvisor;

    public ChatClient client;

    @Test
    @DisplayName("测试, ChatClient 是否可以展示思考过程")
    void test() {
        AbstractAiModelFactory factory = modelProductFactory.getFactory("deepseek");
        ModelProperties properties = ModelEntity.builder()
                .providerCode("deepseek")
                .modelName("deepseek-chat")
                .apiKey(System.getenv("DEEPSEEK_API_KEY"))
                .build();
        client = factory.createClient(properties);

        ThinkRecordLogTools thinkRecordLogTools = new ThinkRecordLogTools();
        DateTimeTools dateTimeTools = new DateTimeTools();

        ToolCallback[] callbacks = ToolCallbacks.from(thinkRecordLogTools, dateTimeTools);

        ChatClientResponse chatClientResponse = client.prompt("秦始皇是哪年大一统的, 距离现在有多少秒. 并记录思考过程")
                .toolCallbacks(callbacks)
                .toolContext(Map.of(
                        "A", 1
                ))
                .system("""
                        你是一个历史知识问答助手，请根据提供的上下文内容回答问题。
                        请使用中文回答。
                        """)
                .advisors(toolCallAdvisor)
                .call()
                .chatClientResponse();

        System.out.println(chatClientResponse.chatResponse().getMetadata().getUsage());

        String content = chatClientResponse.chatResponse().getResult().getOutput().getText();

        System.out.println(content);

    }
}
