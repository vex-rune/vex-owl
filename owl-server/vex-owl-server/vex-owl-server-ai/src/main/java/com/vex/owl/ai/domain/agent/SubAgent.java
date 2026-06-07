package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import reactor.core.publisher.Flux;

/**
 * Agent 接口
 *
 * <p>定义 Agent 的基本行为</p>
 */
public interface Agent {

    /**
     * 获取 Agent 定义
     *
     * @return Agent 定义
     */
    AgentDefinition getDefinition();

    /**
     * 处理输入
     *
     * @param input      输入
     * @param runContext 上下文
     * @return 输出结果
     */
    String process(String input, RunContext runContext);

    /**
     * 流式处理输入
     *
     * @param input      输入
     * @param runContext 上下文
     * @return 流式输出
     */
    default Flux<String> stream(String input, RunContext runContext) {
        return Flux.just(process(input, runContext));
    }
}
