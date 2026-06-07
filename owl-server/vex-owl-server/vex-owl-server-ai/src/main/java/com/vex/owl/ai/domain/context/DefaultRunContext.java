package com.vex.owl.ai.domain.context;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 默认 AI 上下文实现
 *
 * <p>可变对象，使用 withXxx 方法在原实例上修改并返回 this（链式调用）</p>
 */
@Getter
public class DefaultRunContext implements RunContext {

    // === 基础 ===
    private final String id;

    // === 头部/参数 ===
    private Map<String, String> headers;
    private Map<String, String> params;

    // === 目标 ===
    private String method;
    private String tenantId;
    private String sessionId;

    // === 模型配置 ===
    private String provider;
    private String modelName;

    // === 执行状态 ===
    private int step;
    private final long startTime;
    private String previousResult;

    // === 元素 ===
    private final Map<String, Object> attributes = new HashMap<>();

    @Builder
    private DefaultRunContext(String id, String method,
                             Map<String, String> headers, Map<String, String> params,
                             String tenantId, String sessionId,
                             String provider, String modelName,
                             int step, long startTime, String previousResult) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.method = method;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.params = params != null ? new HashMap<>(params) : new HashMap<>();
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.provider = provider;
        this.modelName = modelName;
        this.step = step > 0 ? step : 1;
        this.startTime = startTime > 0 ? startTime : System.currentTimeMillis();
        this.previousResult = previousResult;
    }

    /**
     * 创建默认上下文
     */
    public static DefaultRunContext of(String method, String tenantId) {
        return new DefaultRunContext(
                UUID.randomUUID().toString(),
                method,
                Map.of(), Map.of(),
                tenantId, null,
                null, null,
                1, System.currentTimeMillis(), null
        );
    }

    /**
     * 创建默认上下文 (带模型)
     */
    public static DefaultRunContext of(String method, String tenantId,
                                       String provider, String modelName) {
        return new DefaultRunContext(
                UUID.randomUUID().toString(),
                method,
                Map.of(), Map.of(),
                tenantId, null,
                provider, modelName,
                1, System.currentTimeMillis(), null
        );
    }

    // === 变更方法（原地修改，返回 this） ===

    @Override
    public DefaultRunContext withStep(int step) {
        this.step = step;
        return this;
    }

    @Override
    public DefaultRunContext withResult(String result) {
        this.previousResult = result;
        return this;
    }

    @Override
    public DefaultRunContext withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public DefaultRunContext withMethod(String method) {
        this.method = method;
        return this;
    }

    @Override
    public DefaultRunContext withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    @Override
    public DefaultRunContext withParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public DefaultRunContext withAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }
}
