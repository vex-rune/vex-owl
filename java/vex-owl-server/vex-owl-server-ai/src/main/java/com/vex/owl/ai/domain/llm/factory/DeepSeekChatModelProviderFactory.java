package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.web.client.RestClient;

/**
 * DeepSeek Provider 工厂
 * <p>基于 Spring AI DeepSeek SDK，将 {@link ModelProperties} 中的连接参数
 * 组装为可调用的 {@link ChatClient}。</p>
 */
public class DeepSeekChatModelProviderFactory implements AbstractAiChatModelFactory {

    /** HTTP 客户端构建器，由外部注入以实现连接池复用 */
    RestClient.Builder restClientBuilder = RestClient.builder();

    /**
     * 创建 DeepSeek ChatClient
     *
     * @param modelProperties 模型连接参数
     * @return ChatClient 实例
     */
    @Override
    public ChatClient createClient(ModelProperties modelProperties) {
        DeepSeekApi deepSeekApi = DeepSeekApi
                .builder()
                .baseUrl(modelProperties.getBaseUrl())
                .apiKey(modelProperties.getApiKey())
                .restClientBuilder(restClientBuilder)
                .build();

        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder()
                .model(modelProperties.getModelName())
                .build();

        DeepSeekChatModel chatModel = new DeepSeekChatModel(
                deepSeekApi,
                chatOptions,
                DefaultToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.create()
        );

        return ChatClient.builder(chatModel).build();
    }
}
