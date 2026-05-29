package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * MiniMax Provider 工厂
 * <p>基于 Spring AI MiniMax SDK，将 {@link ModelProperties} 中的连接参数
 * 组装为可调用的 {@link ChatClient}。</p>
 */
public class MiniMaxModelProviderFactory implements AbstractAiModelFactory {

    /**
     * HTTP 客户端构建器，默认使用 RestClient.builder()
     */
    RestClient.Builder restClientBuilder = RestClient.builder();

    /**
     * 创建 MiniMax ChatClient
     *
     * @param modelProperties 模型连接参数
     * @return ChatClient 实例
     */
    @Override
    public ChatClient createClient(ModelProperties modelProperties) {
        MiniMaxApi miniMaxApi = new MiniMaxApi(
                modelProperties.getBaseUrl(),
                modelProperties.getApiKey(),
                restClientBuilder
        );
        MiniMaxChatOptions chatOptions = MiniMaxChatOptions
                .builder()
                .model(modelProperties.getModelName())
                .build();

        MiniMaxChatModel chatModel = new MiniMaxChatModel(
                miniMaxApi,
                chatOptions,
                DefaultToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.create(),
                new DefaultToolExecutionEligibilityPredicate()
        );
        return ChatClient.builder(chatModel).defaultToolContext(
                Map.of(
                        // 平台
                        "platform", "MiniMax",
                        // 模型
                        "model", modelProperties.getModelName()
                )
        ).build();
    }
}
