package com.vex.owl.ai.domain.model.service;

/**
 * 路由上下文
 * <p>封装路由决策所需的辅助信息。作为 {@link com.vex.owl.ai.domain.model.router.ChatRouter#route}
 * 方法的入参，在路由链各策略间传递，各策略按需读取其携带的字段。</p>
 */
public class RouteContext {

    /** 用户原始消息，供 KeywordRouter 做关键词匹配 */
    private final String userMessage;

    /** 主AI模型 ID，供 PrimaryRouter 做回退判断 */
    private final String primaryModelId;

    /**
     * @param userMessage    用户原始消息
     * @param primaryModelId 主AI模型 ID
     */
    public RouteContext(String userMessage, String primaryModelId) {
        this.userMessage = userMessage;
        this.primaryModelId = primaryModelId;
    }

    /**
     * 获取用户原始消息
     *
     * @return 用户输入文本
     */
    public String getUserMessage() {
        return userMessage;
    }

    /**
     * 获取主AI模型 ID
     *
     * @return 主AI的模型 ID
     */
    public String getPrimaryModelId() {
        return primaryModelId;
    }
}
