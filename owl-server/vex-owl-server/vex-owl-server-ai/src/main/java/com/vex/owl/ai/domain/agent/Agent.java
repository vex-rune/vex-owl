package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import reactor.core.publisher.Flux;

/**
 * Agent 接口
 *
 * <p>Agent 是 ChatClient 的模板，持有 ChatClient、advisors、tools、system prompt 等配置。
 * 通过 prompt() 创建请求构建器，在构建器上追加本次请求的 messages，再执行。</p>
 */
public interface Agent {

    /**
     * 获取 Agent 定义
     */
    AgentDefinition getDefinition();

    /**
     * 创建请求构建器（在构建器上追加 messages 后调用 call/stream 执行）
     */
    AgentRequest prompt(String input);
}
