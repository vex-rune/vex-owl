package com.vex.owl.ai.domain.model.factory;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.vex.owl.ai.domain.model.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.web.client.RestClient;

/**
 * 通义千问 Provider 工厂
 * <p>基于 Spring AI Alibaba DashScope SDK，将 {@link ModelProperties} 中的连接参数
 * 组装为可调用的 {@link ChatClient}。</p>
 */
public class DashScopeChatModelProviderFactory implements AbstractAiChatModelFactory {

    /** HTTP 客户端构建器，由外部注入以实现连接池复用 */
    RestClient.Builder restClientBuilder;

    /**
     * 创建通义千问 ChatClient
     *
     * @param modelProperties 模型连接参数
     * @return ChatClient 实例
     */
    @Override
    public ChatClient createClient(ModelProperties modelProperties) {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(modelProperties.getApiKey())
                .baseUrl(modelProperties.getBaseUrl())
                .restClientBuilder(restClientBuilder)
                .build();

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .model(modelProperties.getModelName())
                .build();

        DashScopeChatModel chatModel = new DashScopeChatModel(
                dashScopeApi,
                chatOptions,
                DefaultToolCallingManager.builder().build(),
                RetryUtils.DEFAULT_RETRY_TEMPLATE,
                ObservationRegistry.create()
        );

        return ChatClient.builder(chatModel).build();
    }
}
