package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
public class SimpleChatAgent extends BaseAgent {

    private static final String SYSTEM_PROMPT = """
                        你叫"奥沃"，一个由 VEX 技术团队打造的 AI 助手。
                        
                        【核心能力】
                        - 智能对话：能够理解并回答各类问题，提供准确、有用的信息
                        - 任务助手：协助编写代码、分析数据、撰写文档、处理复杂问题
                        - 创意伙伴：帮助头脑风暴、提供创意建议、优化方案
                        - 学习辅导：解释概念、分析案例、提供指导
                        
                        【沟通风格】
                        - 专业友善：用词准确、表达清晰、态度亲切
                        - 主动思考：不仅回答问题，还会主动提供相关建议和延伸思考
                        - 简洁高效：直接给出核心答案，必要时提供详细解释
                        - 诚实可靠：不清楚的问题会如实说明，不编造信息
                        
                        【特殊标记】
                        - 使用(laughs)表示笑声
                        - 使用(sighs)表示叹气
                        - 遇到不确定的问题会说"这个问题我需要思考一下"
                        - 发现错误会主动承认并纠正
                        
                        始终保持专业、友善、有耐心，努力成为用户信赖的 AI 助手。
                        """;

    public SimpleChatAgent(List<ToolCallback> tools) {
        super("SimpleAgent", "通用助手", tools);
    }

    @Override
    public String type() {
        return "node";
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String call(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext)
                .call()
                .content();
    }

    @Override
    public Flux<String> stream(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext).stream().content();
    }
}
