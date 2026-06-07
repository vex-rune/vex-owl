package com.vex.owl.ai.domain.context;

import java.util.Map;

/**
 * AI 公共上下文接口
 *
 * <p>参考 Spring ServerHttpRequest 设计风格，统一贯穿整个请求生命周期</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>只读方法获取信息，变更方法返回新实例（不可变）</li>
 *   <li>状态流转由 TaskStateMachine 处理，上下文只存储数据</li>
 *   <li>所有组件使用同一个上下文实例</li>
 * </ul>
 */
public interface AIContext {
    
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
    
    // ==================== 变更方法 ====================
    
    /**
     * 设置步骤
     */
    AIContext withStep(int step);
    
    /**
     * 设置上一步结果
     */
    AIContext withResult(String result);
    
    /**
     * 设置会话ID
     */
    AIContext withSessionId(String sessionId);
    
    /**
     * 设置方法
     */
    AIContext withMethod(String method);
    
    /**
     * 设置路径
     */
    AIContext withPath(String path);
    
    /**
     * 添加请求头
     */
    AIContext withHeader(String key, String value);
    
    /**
     * 添加查询参数
     */
    AIContext withParam(String key, String value);
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取经过的毫秒数
     */
    default long elapsedMs() {
        return System.currentTimeMillis() - getStartTime();
    }
}
