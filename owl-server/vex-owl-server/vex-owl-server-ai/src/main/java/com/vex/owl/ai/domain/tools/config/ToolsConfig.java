package com.vex.owl.ai.domain.tools.config;

import com.vex.owl.ai.app.ToolCallingListener;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.SpringBeanToolCallbackResolver;
import org.springframework.ai.util.json.schema.SchemaType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
@RequiredArgsConstructor
public class ToolsConfig {

    private final GenericApplicationContext genericApplicationContext;

    // 自动注入 Spring AI 内置的工具调用管理器
    @Bean
    public ToolCallAdvisor toolCallAdvisor(ToolCallingManager toolCallingManager, 
                                          ApplicationEventPublisher publisher) {
        ToolCallingListener filterToolCallingManager = new ToolCallingListener(toolCallingManager, publisher);

        ToolCallAdvisor advisor = ToolCallAdvisor.builder()
                .toolCallingManager(filterToolCallingManager)
                .advisorOrder(1)
                .build();

        return advisor;
    }

    @Bean
    public ToolCallingManager toolCallingManager(DefaultToolExecutionExceptionProcessor processor) {
        // 默认实现，足够你现在用
        return DefaultToolCallingManager.builder()
                .observationRegistry(ObservationRegistry.create())
                .toolCallbackResolver(SpringBeanToolCallbackResolver.builder()
                        .applicationContext(genericApplicationContext)
                        .schemaType(SchemaType.JSON_SCHEMA)
                        .build())
                .toolExecutionExceptionProcessor(processor)
                .build();
    }

    @Bean
    private static DefaultToolExecutionExceptionProcessor defaultToolExecutionExceptionProcessor() {
        DefaultToolExecutionExceptionProcessor build = DefaultToolExecutionExceptionProcessor.builder()
                .build();
        return build;
    }
}