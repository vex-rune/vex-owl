package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import org.springframework.ai.chat.client.ChatClient;
import reactor.core.publisher.Flux;

/**
 * Agent 接口
 *
 * <p>Agent 是 ChatClient 的模板，持有 system prompt、advisors、tools 等配置。
 * 调用方传入 ChatClient，Agent 在其上追加本次请求的配置后执行。</p>
 *
 * @param <T> 返回类型，大多数 Agent 返回 String，特殊 Agent 可返回结构化类型
 */
public interface Agent<T> {

    AgentDefinition getDefinition();

    String type();

    T call(String input, ChatClient client, RunContext runContext);

    Flux<T> stream(String input, ChatClient client, RunContext runContext);
}
