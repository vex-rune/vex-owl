package com.vex.owl.ai.domain.context;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import org.springframework.ai.chat.client.ChatClientRequest;

import java.util.Map;

/**
 * ChatClientRequest Context → RunContext 转换器
 */
public final class RunContextConverter {

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION_ID = "sessionId";

    private RunContextConverter() {}

    /**
     * 从 ChatClientRequest 提取上下文构建 RunContext
     *
     * <p>ChatClientRequest.context() 中应包含 userId、sessionId 等信息，
     * 由 DefaultAiManager 在创建 ChatClient 时通过 .defaultToolContext() 注入。</p>
     *
     * @param request Spring AI 的 ChatClientRequest
     * @param modelProperties 模型配置
     * @return RunContext
     */
    public static RunContext fromRequest(ChatClientRequest request, ModelProperties modelProperties) {
        Map<String, Object> ctx = request != null ? request.context() : Map.of();

        String userId = getString(ctx, KEY_USER_ID);
        String sessionId = getString(ctx, KEY_SESSION_ID);

        return RunContext.builder()
                .userId(userId)
                .sessionId(sessionId)
                .modelProperties(modelProperties)
                .build();
    }

    /**
     * 从 Map 提取上下文构建 RunContext
     *
     * <p>适用于直接传入 context map 的场景。</p>
     *
     * @param context 上下文 Map
     * @param modelProperties 模型配置
     * @return RunContext
     */
    public static RunContext fromMap(Map<String, Object> context, ModelProperties modelProperties) {
        String userId = getString(context, KEY_USER_ID);
        String sessionId = getString(context, KEY_SESSION_ID);

        return RunContext.builder()
                .userId(userId)
                .sessionId(sessionId)
                .modelProperties(modelProperties)
                .build();
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}