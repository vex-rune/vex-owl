package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.event.TokenUsageAdvisor;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI聊天模型工厂门面
 * <p>根据 providerCode 分派到对应的具体工厂实现。
 * 上游调用方无需感知具体 Provider 类型，
 * 只需传入 code 即可获取 {@link AbstractAiModelFactory} 实例。</p>
 *
 * <p>支持的 Provider：
 * <ul>
 *   <li>{@code dashscope} — 通义千问（DashScopeChatModelProviderFactory）</li>
 *   <li>{@code deepseek} — DeepSeek（DeepSeekChatModelProviderFactory）</li>
 *   <li>{@code minimax} — MiniMax（MiniMaxChatModelProviderFactory）</li>
 * </ul></p>
 */
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ModelProductFactory {

    private TokenUsageAdvisor tokenUsageAdvisor;

    /**
     * 根据 Provider 代码获取对应的模型工厂实例
     *
     * @param providerCode Provider 标识码，如 "dashscope"、"deepseek"、"minimax"
     * @return 匹配的工厂实例；code 为空或未匹配时返回 null
     */
    public AbstractAiModelFactory getFactory(String providerCode) {
        if (providerCode == null) {
            return null;
        }
        AbstractAiModelFactory factory = switch (providerCode) {
            case "dashscope" -> new DashScopeModelProviderFactory();
            case "deepseek" -> new DeepSeekModelProviderFactory();
            case "minimax" -> new MiniMaxModelProviderFactory();
            default -> throw new IllegalArgumentException("providerCode=" + providerCode + ", 没有对应的工厂");
        };

        return new TokenUsageAdvisorProviderFactoryWapper(factory, tokenUsageAdvisor);
    }

}
