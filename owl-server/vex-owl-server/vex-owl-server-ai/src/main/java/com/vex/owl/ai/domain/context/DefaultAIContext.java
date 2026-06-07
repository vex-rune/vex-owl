package com.vex.owl.ai.domain.context;

import java.util.Map;
import java.util.UUID;

/**
 * 默认 AI 上下文实现
 *
 * <p>使用 record 实现，不可变</p>
 */
public record DefaultAIContext(
    String id,
    String method,
    String path,
    Map<String, String> headers,
    Map<String, String> params,
    String tenantId,
    String sessionId,
    String provider,
    String modelName,
    int step,
    long startTime,
    String previousResult
) implements AIContext {

    /**
     * 创建默认上下文
     */
    public static DefaultAIContext of(String method, String path, String tenantId) {
        return new DefaultAIContext(
            UUID.randomUUID().toString(),
            method,
            path,
            Map.of(),
            Map.of(),
            tenantId,
            null,
            null,
            null,
            1,
            System.currentTimeMillis(),
            null
        );
    }

    /**
     * 创建默认上下文 (带模型)
     */
    public static DefaultAIContext of(String method, String path, String tenantId,
                                     String provider, String modelName) {
        return new DefaultAIContext(
            UUID.randomUUID().toString(),
            method,
            path,
            Map.of(),
            Map.of(),
            tenantId,
            null,
            provider,
            modelName,
            1,
            System.currentTimeMillis(),
            null
        );
    }

    @Override
    public DefaultAIContext withStep(int step) {
        return new DefaultAIContext(
            id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult
        );
    }

    @Override
    public DefaultAIContext withResult(String result) {
        return new DefaultAIContext(
            id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, result
        );
    }

    /**
     * 设置会话ID
     */
    public DefaultAIContext withSessionId(String sessionId) {
        return new DefaultAIContext(
            id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult
        );
    }

    /**
     * 添加请求头
     */
    public DefaultAIContext withHeader(String key, String value) {
        Map<String, String> newHeaders = new java.util.HashMap<>(headers());
        newHeaders.put(key, value);
        return new DefaultAIContext(
            id, method, path, newHeaders, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult
        );
    }

    /**
     * 添加查询参数
     */
    public DefaultAIContext withParam(String key, String value) {
        Map<String, String> newParams = new java.util.HashMap<>(params());
        newParams.put(key, value);
        return new DefaultAIContext(
            id, method, path, headers, newParams,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult
        );
    }
}