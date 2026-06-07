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
    String id();
    
    /**
     * 方法: chat / plan / orchestrate
     */
    String method();
    
    /**
     * 路径: /ai/chat / /ai/plan / ...
     */
    String path();
    
    // ==================== 头部/参数 ====================
    
    /**
     * 请求头
     */
    Map<String, String> headers();
    
    /**
     * 查询参数
     */
    Map<String, String> params();
    
    // ==================== 目标信息 ====================
    
    /**
     * 租户ID
     */
    String tenantId();
    
    /**
     * 会话ID
     */
    String sessionId();
    
    // ==================== 模型配置 ====================
    
    /**
     * 模型提供商
     */
    String provider();
    
    /**
     * 模型名称
     */
    String modelName();
    
    // ==================== 执行状态 ====================
    
    /**
     * 当前步骤
     */
    int step();
    
    /**
     * 开始时间 (毫秒)
     */
    long startMs();
    
    /**
     * 上一步结果 (Pipeline 传递)
     */
    String previousResult();
    
    // ==================== 变更方法 ====================
    
    /**
     * 设置步骤
     */
    AIContext withStep(int step);
    
    /**
     * 设置上一步结果
     */
    AIContext withResult(String result);
    
    // ==================== 便捷方法 ====================
    
    /**
     * 获取执行耗时 (毫秒)
     */
    default long elapsedMs() {
        return System.currentTimeMillis() - startMs();
    }
    
    /**
     * 获取请求头
     */
    default String header(String key) {
        return headers() != null ? headers().get(key) : null;
    }
    
    /**
     * 获取查询参数
     */
    default String param(String key) {
        return params() != null ? params().get(key) : null;
    }
}