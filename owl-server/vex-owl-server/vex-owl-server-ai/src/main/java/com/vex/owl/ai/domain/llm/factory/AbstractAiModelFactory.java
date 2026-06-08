package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import org.springframework.ai.chat.model.ChatModel;

/**
 * AI聊天模型工厂抽象接口
 * <p>定义统一的多模型工厂契约。每个具体的 Provider 实现此接口，
 * 负责根据模型配置创建对应的 {@link ChatModel} 实例。</p>
 */
public interface AbstractAiModelFactory {

    /**
     * 根据模型配置创建 ChatModel
     *
     * @param modelProperties 模型连接参数（apiKey、baseUrl、modelName）
     * @return ChatModel 实例
     */
    ChatModel createModel(ModelProperties modelProperties);
}
