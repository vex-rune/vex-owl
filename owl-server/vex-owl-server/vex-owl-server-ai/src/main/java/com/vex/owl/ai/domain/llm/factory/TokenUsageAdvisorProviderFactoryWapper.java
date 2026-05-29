package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.event.TokenUsageAdvisor;
import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.retry.RetryUtils;

import java.util.Map;

@AllArgsConstructor
public class TokenUsageAdvisorProviderFactoryWapper implements AbstractAiModelFactory {

    private final AbstractAiModelFactory factory;
    private final TokenUsageAdvisor tokenUsageAdvisor;

    /**
     * 装饰器模式, 增强 token用量的事件发送器
     */
    @Override
    public ChatClient createClient(ModelProperties modelProperties) {
        ChatClient client = factory.createClient(modelProperties);

        // 用法：mutate() = 复制 + 改配置 + 构建新客户端
        return client.mutate()
                .defaultAdvisors(tokenUsageAdvisor)
                .build();
    }
}
