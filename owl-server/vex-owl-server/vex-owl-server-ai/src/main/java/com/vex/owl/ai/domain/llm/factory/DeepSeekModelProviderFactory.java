package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.web.client.RestClient;

/**
 * DeepSeek Provider 工厂
 */
public class DeepSeekModelProviderFactory implements AbstractAiModelFactory {

    private final RestClient.Builder restClientBuilder = RestClient.builder();

    @Override
    public ChatModel createModel(ModelProperties modelProperties) {
        DeepSeekApi.Builder builder = DeepSeekApi.builder();
        String baseUrl = modelProperties.getBaseUrl();
        if (baseUrl != null) {
            builder.baseUrl(baseUrl);
        }
        builder.restClientBuilder(restClientBuilder);
        DeepSeekApi deepSeekApi = builder
                .apiKey(modelProperties.getApiKey())
                .build();

        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder()
                .model(modelProperties.getModelName())
                .build();

        return new DeepSeekChatModel(
                deepSeekApi,
                chatOptions,
                DefaultToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.create()
        );
    }
}
