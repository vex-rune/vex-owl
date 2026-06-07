package com.vex.owl.ai.domain.context;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 默认 AI 上下文实现
 *
 * <p>不可变对象，使用 withXxx 方法返回新实例</p>
 */
@Getter
public class DefaultAIContext implements AIContext {

    // === 基础 ===
    private final String id;
    private final String method;
    private final String path;

    // === 头部/参数 ===
    private final Map<String, String> headers;
    private final Map<String, String> params;

    // === 目标 ===
    private final String tenantId;
    private final String sessionId;

    // === 模型配置 ===
    private final String provider;
    private final String modelName;

    // === 执行状态 ===
    private final int step;
    private final long startTime;
    private final String previousResult;

    /**
     * 私有构造函数
     */
    private DefaultAIContext(String id, String method, String path,
                            Map<String, String> headers, Map<String, String> params,
                            String tenantId, String sessionId,
                            String provider, String modelName,
                            int step, long startTime, String previousResult) {
        this.id = id;
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.params = params;
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.step = step;
        this.startTime = startTime;
        this.previousResult = previousResult;
    }

    /**
     * 创建默认上下文
     */
    public static DefaultAIContext of(String method, String path, String tenantId) {
        return new DefaultAIContext(
            UUID.randomUUID().toString(),
            method, path,
            Map.of(), Map.of(),
            tenantId, null,
            null, null,
            1, System.currentTimeMillis(), null
        );
    }

    /**
     * 创建默认上下文 (带模型)
     */
    public static DefaultAIContext of(String method, String path, String tenantId,
                                     String provider, String modelName) {
        return new DefaultAIContext(
            UUID.randomUUID().toString(),
            method, path,
            Map.of(), Map.of(),
            tenantId, null,
            provider, modelName,
            1, System.currentTimeMillis(), null
        );
    }

    // === 变更方法 ===

    @Override
    public AIContext withStep(int step) {
        return new DefaultAIContext(id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }

    @Override
    public AIContext withResult(String result) {
        return new DefaultAIContext(id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, result);
    }

    @Override
    public AIContext withSessionId(String sessionId) {
        return new DefaultAIContext(id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }

    @Override
    public AIContext withMethod(String method) {
        return new DefaultAIContext(id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }

    @Override
    public AIContext withPath(String path) {
        return new DefaultAIContext(id, method, path, headers, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }

    @Override
    public AIContext withHeader(String key, String value) {
        Map<String, String> newHeaders = new HashMap<>(headers);
        newHeaders.put(key, value);
        return new DefaultAIContext(id, method, path, newHeaders, params,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }

    @Override
    public AIContext withParam(String key, String value) {
        Map<String, String> newParams = new HashMap<>(params);
        newParams.put(key, value);
        return new DefaultAIContext(id, method, path, headers, newParams,
            tenantId, sessionId, provider, modelName,
            step, startTime, previousResult);
    }
}
