package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;

/**
 * MiniMax Provider 工厂
 */
public class MiniMaxModelProviderFactory implements AbstractAiModelFactory {

    @Override
    public ChatModel createModel(ModelProperties modelProperties) {
        MiniMaxApi miniMaxApi = new MiniMaxApi(modelProperties.getApiKey());
        MiniMaxChatOptions chatOptions = MiniMaxChatOptions
                .builder()
                .model(modelProperties.getModelName())
                .build();

        return new MiniMaxChatModel(
                miniMaxApi,
                chatOptions,
                DefaultToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.create(),
                new DefaultToolExecutionEligibilityPredicate()
        );
    }
}
