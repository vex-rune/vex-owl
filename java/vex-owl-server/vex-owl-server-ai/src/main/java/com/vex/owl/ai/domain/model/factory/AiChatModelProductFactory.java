package com.vex.owl.ai.domain.model.factory;

/**
 * AI聊天模型工厂门面
 * <p>根据 providerCode 分派到对应的具体工厂实现。
 * 上游（ChatAppService）无需感知具体 Provider 类型，
 * 只需传入 code 即可获取 {@link AbstractAiChatModelFactory} 实例。</p>
 *
 * <p>支持的 Provider：
 * <ul>
 *   <li>{@code dashscope} — 通义千问（DashScopeChatModelProviderFactory）</li>
 *   <li>{@code deepseek} — DeepSeek（DeepSeekChatModelProviderFactory）</li>
 *   <li>{@code minimax} — MiniMax（MiniMaxChatModelProviderFactory）</li>
 * </ul></p>
 */
public class AiChatModelProductFactory {

    /**
     * 根据 Provider 代码获取对应的模型工厂实例
     *
     * @param providerCode Provider 标识码，如 "dashscope"、"deepseek"、"minimax"
     * @return 匹配的工厂实例；code 为空或未匹配时返回 null
     */
    public AbstractAiChatModelFactory get(String providerCode) {
        if (providerCode == null) {
            return null;
        }
        return switch (providerCode) {
            case "dashscope" -> new DashScopeChatModelProviderFactory();
            case "deepseek" -> new DeepSeekChatModelProviderFactory();
            case "minimax" -> new MiniMaxChatModelProviderFactory();
            default -> null;
        };
    }
}
