package com.vex.owl.ai.domain.context;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import lombok.Builder;
import lombok.Data;
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
@Data
@Builder
public class RunContext {

    @Getter
    private String id;
    @Getter
    private Map<String, String> headers;
    @Getter
    private Map<String, String> params;
    @Getter
    private String userId;
    @Getter
    private String sessionId;
    @Getter
    private ModelProperties modelProperties;
    @Getter
    @Builder.Default
    private int step = 1;
    @Getter
    @Builder.Default
    private long startTime = System.currentTimeMillis();
    @Getter
    private String previousResult;

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
        map.put("id", id!=null?id:"");
        map.put("headers", headers!=null?headers:"");
        map.put("params", params!=null?params:"");
        map.put("userId", userId!=null?userId:"");
        map.put("sessionId", sessionId!=null?sessionId:"");
        map.put("step", step);
        map.put("startTime", startTime);
        map.put("provider", modelProperties.getProviderCode());
        map.put("model", modelProperties.getModelName());
        return map;
    }
}
