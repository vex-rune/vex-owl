package com.vex.owl.ai.domain.llm.repo;

/**
 * 模型连接属性接口
 * <p>定义 Provider Factory 创建 ChatClient 所需的最小参数集合。
 * 任何需要被 Factory 消费的模型配置实体都应实现此接口。</p>
 */
public interface ModelProperties {

    /**
     * 获取 API 密钥
     *
     * @return 模型提供商的 API Key
     */
    String getApiKey();

    /**
     * 获取模型名称
     *
     * @return 模型标识名，如 "qwen-plus"、"deepseek-chat"
     */
    String getModelName();

    /**
     * 获取 API 基础地址
     *
     * @return 模型提供商的 API Base URL
     */
    String getBaseUrl();
}
