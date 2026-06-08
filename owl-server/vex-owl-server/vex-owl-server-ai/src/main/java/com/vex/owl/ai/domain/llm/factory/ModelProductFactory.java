package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.app.agent.TokenUsageAdvisor;
import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.stereotype.Component;

/**
 * AI 聊天模型工厂门面
 *
 * <p>根据 providerCode 创建对应的 ChatModel，注入 ToolCallingManager，
 * 返回组装好的 {@link ChatClient}，调用方无需关心工厂细节。</p>
 */
@Component
@AllArgsConstructor
public class ModelProductFactory {

    private final TokenUsageAdvisor tokenUsageAdvisor;
    private final ToolCallingManager toolCallingManager;

    /**
     * 系统 Prompt 强约束：ReAct 格式
     *
     * <p>从工厂出来的每个 ChatClient 天生携带此约束。
     * 强制模型按 Thought → Action 格式输出，禁止省略思考过程。</p>
     */
    private static final String REACT_SYSTEM_PROMPT = """
            【强制输出格式】
            你必须严格按下面格式输出，禁止省略任何部分：

            Thought: <你的思考过程，分析现状、问题、下一步>
            Action: <工具名>(<参数>)

            规则：
            1. 每次回复必须以 "Thought:" 开头
            2. Thought 中必须包含：当前现状分析、面临的问题、下一步计划
            3. Action 中必须是合法的工具调用，参数要完整
            4. 禁止省略 Thought，禁止跳过思考直接输出答案
            5. 如果没有合适的工具可用，在 Thought 中说明，并给出最终回答""";

    /**
     * 根据 providerCode 创建 ChatClient
     *
     * @param providerCode    Provider 标识（如 "deepseek"、"minimax"）
     * @param modelProperties 模型连接参数
     * @return 组装好的 ChatClient
     */
    public ChatClient createClient(String providerCode, ModelProperties modelProperties) {
        AbstractAiModelFactory factory = getFactory(providerCode);
        return ChatClient
                .builder(factory.createModel(modelProperties))
                .defaultSystem(REACT_SYSTEM_PROMPT)
                .defaultAdvisors(tokenUsageAdvisor, ToolCallAdvisor.builder()
                        .advisorOrder(1)
                        .toolCallingManager(toolCallingManager)
                        .build())
                .build();
    }

    private AbstractAiModelFactory getFactory(String providerCode) {
        if (providerCode == null) {
            throw new IllegalArgumentException("providerCode 不能为空");
        }

        return switch (providerCode) {
            case "deepseek" -> new DeepSeekModelProviderFactory();
            case "minimax" -> new MiniMaxModelProviderFactory();
            default -> throw new IllegalArgumentException("providerCode=" + providerCode + ", 没有对应的工厂");
        };
    }
}
