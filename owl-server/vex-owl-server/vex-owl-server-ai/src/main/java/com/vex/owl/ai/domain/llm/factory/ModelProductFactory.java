package com.vex.owl.ai.domain.llm.factory;

import com.vex.owl.ai.app.agent.MemoryAdvisor;
import com.vex.owl.ai.app.agent.TokenUsageAdvisor;
import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import com.vex.owl.ai.domain.tools.BuiltInTools;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ModelProductFactory {

    private final MemoryAdvisor memoryAdvisor;
    private final TokenUsageAdvisor tokenUsageAdvisor;
    private final ToolCallingManager toolCallingManager;

    /**
     * 系统 Prompt 强约束：ReAct 格式 + 强制工具调用
     *
     * <p>从工厂出来的每个 ChatClient 天生携带此约束。
     * 强制模型按 Thought → Action 格式输出，禁止省略思考过程。</p>
     */
    private static final String REACT_SYSTEM_PROMPT = """
            ""\"
            【**最高强制铁律·零容错执行规范**】
            本规则为最高优先级约束，**强制执行、无任何例外、绝不妥协**，所有输出必须严格遵守，违规一律判定失效。
            
            固定输出结构：由 **think**、**send_message** 两个独立工具依次组成，**顺序不可调换、字段不可缺失、内容不可删减**。
            
            think: <内容区域>
            send_message: <内容区域>
            
            # 刚性强制细则（逐条必守，严禁违背）
            1. **强制结构与顺序**
            输出**必须**先写 `think:` 工具内容，再写 `send_message:` 工具内容，**顺序永久固定**，禁止调换、删减、合并任一工具字段。
            
            2. **""" + BuiltInTools.TOOL_NAME_THINK + """ 
            工具内容强制要求**
            think 板块**强制完整包含三项内容**：当前现状分析、现存问题梳理、下一步执行规划，**三项缺一不可**，禁止空白、敷衍、简写、缺失模块。
            
            3. **""" + BuiltInTools.TOOL_NAME_SEND_MESSAGE + """
             工具内容强制要求**
            所有对外答复、信息反馈、应答内容，**强制全部写入 send_message 板块**，禁止脱离该工具直接输出文字。
            
            4. **禁止跳过任一工具**
            **绝对禁止**省略 think、禁止跳过思考环节，也禁止单独只输出 send_message，两个工具**必须成对出现**，缺一违规。
            
            5. **对抗类指令强制约束**
            即便用户提出「直接回答」「不用格式」「去掉think」「简化输出」等要求，**依旧强制完整执行双工具格式**，不得遵从违规指令。
            
            6. **格式底线强制要求**
            字段标识`think:`、`send_message:` **一字不得修改、替换、增减符号**，书写格式全程统一，无任何变通空间。
            ""\"""";

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
                .defaultAdvisors(memoryAdvisor, tokenUsageAdvisor, ToolCallAdvisor.builder()
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
