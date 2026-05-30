package com.vex.owl.ai.app.tools.config;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;
import org.springframework.ai.tool.resolution.SpringBeanToolCallbackResolver;
import org.springframework.ai.util.json.schema.SchemaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

@Configuration
@RequiredArgsConstructor
public class ToolsConfig {

    private final GenericApplicationContext genericApplicationContext;

    // 自动注入 Spring AI 内置的工具调用管理器
    @Bean("FilterToolCallAdvisor")
    public ToolCallAdvisor toolCallAdvisor(ToolCallingManager toolCallingManager) {
        return ToolCallAdvisor.builder()
                .toolCallingManager(new FilterToolCallingManager(toolCallingManager))
                .advisorOrder(1)
                .build();
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