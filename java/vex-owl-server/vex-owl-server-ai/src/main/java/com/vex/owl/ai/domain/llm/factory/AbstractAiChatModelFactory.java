package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import org.springframework.ai.chat.client.ChatClient;

/**
 * AI聊天模型工厂抽象接口
 * <p>定义统一的多模型工厂契约。每个具体的 Provider 实现此接口，
 * 负责根据模型配置创建对应的 Spring AI {@link ChatClient} 实例。</p>
 */
public interface AbstractAiChatModelFactory {

    /**
     * 根据模型配置创建 ChatClient
     *
     * @param modelProperties 模型连接参数（apiKey、baseUrl、modelName）
     * @return 可调用的 ChatClient，通过 {@code client.prompt(msg).call()} 发起对话
     */
    ChatClient createClient(ModelProperties modelProperties);
}
