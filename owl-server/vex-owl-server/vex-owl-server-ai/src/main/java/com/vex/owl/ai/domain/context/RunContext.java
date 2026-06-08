package com.vex.owl.ai.domain.context;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI 执行上下文
 *
 * <p>纯数据对象，不持有任何 Spring Bean 引用。
 * 需要获取 AiManager/ModelProductFactory 时通过方法参数传入。</p>
 */
public class RunContext {

    @Getter
    private final String id;
    @Getter
    private Map<String, String> headers;
    @Getter
    private Map<String, String> params;
    @Getter
    private String tenantId;
    @Getter
    private String sessionId;
    @Getter
    private final ModelProperties modelProperties;
    @Getter
    private int step;
    @Getter
    private final long startTime;
    @Getter
    private String previousResult;

    @Builder
    public RunContext(ModelProperties modelProperties,
                      Map<String, String> headers,
                      Map<String, String> params,
                      String tenantId,
                      String sessionId) {
        this.id = UUID.randomUUID().toString();
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.params = params != null ? new HashMap<>(params) : new HashMap<>();
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.modelProperties = modelProperties;
        this.step = 1;
        this.startTime = System.currentTimeMillis();
    }

    public RunContext addStep() {
        this.step++;
        return this;
    }

    public RunContext withResult(String result) {
        this.previousResult = result;
        return this;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("headers", headers);
        map.put("params", params);
        map.put("tenantId", tenantId);
        map.put("sessionId", sessionId);
        map.put("step", step);
        map.put("startTime", startTime);
        return map;
    }
}
