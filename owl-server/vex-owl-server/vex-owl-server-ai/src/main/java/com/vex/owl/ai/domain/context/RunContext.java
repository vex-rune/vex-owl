package com.vex.owl.ai.domain.context;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 公共上下文接口
 *
 * <p>参考 Spring ServerHttpRequest 设计风格，统一贯穿整个请求生命周期</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>只读方法获取信息，变更方法在原实例上修改并返回 this（可变，支持链式调用）</li>
 *   <li>状态流转由 TaskStateMachine 处理，上下文只存储数据</li>
 *   <li>所有组件使用同一个上下文实例</li>
 * </ul>
 */
public interface RunContext {


    // ==================== 基础标识 ====================

    /**
     * 唯一标识 (traceId)
     */
    String getId();

    /**
     * 方法: chat / plan / orchestrate
     */
    String getMethod();

    // ==================== 头部/参数 ====================

    /**
     * 请求头
     */
    Map<String, String> getHeaders();

    /**
     * 查询参数
     */
    Map<String, String> getParams();

    // ==================== 目标信息 ====================

    /**
     * 租户ID
     */
    String getTenantId();

    /**
     * 会话ID
     */
    String getSessionId();

    // ==================== 模型配置 ====================

    /**
     * 模型提供商
     */
    String getProvider();

    /**
     * 模型名称
     */
    String getModelName();

    // ==================== 执行状态 ====================

    /**
     * 当前步骤
     */
    int getStep();

    /**
     * 开始时间 (毫秒)
     */
    long getStartTime();

    /**
     * 上一步结果 (Pipeline 传递)
     */
    String getPreviousResult();

    Map<String, Object> getAttributes();

    // ==================== 变更方法 ====================

    /**
     * 设置步骤
     */
    RunContext withStep(int step);

    /**
     * 设置上一步结果
     */
    RunContext withResult(String result);

    /**
     * 设置会话ID
     */
    RunContext withSessionId(String sessionId);

    /**
     * 设置方法
     */
    RunContext withMethod(String method);

    /**
     * 添加请求头
     */
    RunContext withHeader(String key, String value);

    /**
     * 添加查询参数
     */
    RunContext withParam(String key, String value);

    /**
     * 添加属性
     */
    RunContext withAttribute(String key, Object value);

    // ==================== 便捷方法 ====================

    /**
     * 获取经过的毫秒数
     */
    default long elapsedMs() {
        return System.currentTimeMillis() - getStartTime();
    }

    /**
     * 转 Map, 为 value=null 跳过
     */
    default Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (getId() != null) {
            map.put("id", getId());
        }
        if (getMethod() != null) {
            map.put("method", getMethod());
        }
        if (getHeaders() != null) {
            map.put("headers", getHeaders());
        }
        if (getParams() != null) {
            map.put("tenantId", getTenantId());
        }

        if (getSessionId() != null) {
            map.put("sessionId", getSessionId());
        }
        if (getProvider() != null) {
            map.put("provider", getProvider());
        }
        if (getModelName() != null) {
            map.put("modelName", getModelName());
        }
        if (getStep() != 0) {
            map.put("step", getStep());
        }
        if (getStartTime() != 0) {
            map.put("startTime", getStartTime());
        }
        if (getPreviousResult() != null) {
            map.put("previousResult", getPreviousResult());
        }
        if (getAttributes() != null) {
            map.put("attributes", getAttributes());
        }
        if (this != null) {
            map.put("content", this);
        }
        return map;
    }

    /**
     * 从 Map 创建上下文
     */
    @SuppressWarnings("unchecked")
    static RunContext fromMap(Map<String, Object> map) {
        DefaultRunContext ctx = DefaultRunContext.builder()
                .id((String) map.get("id"))
                .method((String) map.get("method"))
                .headers((Map<String, String>) map.get("headers"))
                .params((Map<String, String>) map.get("params"))
                .tenantId((String) map.get("tenantId"))
                .sessionId((String) map.get("sessionId"))
                .provider((String) map.get("provider"))
                .modelName((String) map.get("modelName"))
                .step(map.get("step") != null ? (Integer) map.get("step") : 1)
                .startTime(map.get("startTime") != null ? (Long) map.get("startTime") : System.currentTimeMillis())
                .previousResult((String) map.get("previousResult"))
                .build();
        Object attrs = map.get("attributes");
        if (attrs instanceof Map<?, ?> m) {
            ctx.getAttributes().putAll((Map<String, Object>) m);
        }
        return ctx;
    }

    /**
     * 更新 Map 形态的上下文结果, 步数
     */
    static void updateResultByMap(Map<String, Object> contextMap, String previousResult) {
        contextMap.put("previousResult", previousResult);
        contextMap.put("step", contextMap.get("step") != null ? (Integer) contextMap.get("step") : 1); // Ensure step is not null
        Object content = contextMap.get("content");
        if (content != null && content instanceof RunContext) {
            RunContext runContext = (RunContext) content;
            contextMap.put("previousResult", runContext.getPreviousResult());
        }
    }

}
