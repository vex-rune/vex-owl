package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

/**
 * Agent 请求构建器
 *
 * <p>每次 prompt() 创建一个新实例，调用方可追加本次会话的 messages，然后执行</p>
 */
public interface AgentRequest {

    /**
     * 追加本次请求的会话消息
     */
    default AgentRequest assistantMessage(String message) {
        return this;
    }

    /**
     * 追加本次请求的工具
     */
    default AgentRequest tool(ToolCallback... tool) {
        return this;
    }

    /**
     * 执行请求
     */
    String call(RunContext runContext);

    /**
     * 流式执行请求
     */
    Flux<String> stream(RunContext runContext);
}
