package com.vex.owl.ai2.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.dashscope.api.DashscopeApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Bean
    public DashscopeApi dashscopeApi() {
        return DashscopeApi.withApiKey(apiKey);
    }

    @Bean
    public ChatClient dashscopeChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是一个有帮助的AI助手。")
                .build();
    }
}