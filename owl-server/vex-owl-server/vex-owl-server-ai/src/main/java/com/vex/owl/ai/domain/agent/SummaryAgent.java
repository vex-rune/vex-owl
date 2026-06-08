package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
public class SummaryAgent extends BaseAgent {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的总结专家。你的职责是对提供的内容进行精准总结。

            ## 工作原则
            - 提炼核心要点，去除冗余信息
            - 保持逻辑结构清晰，层次分明
            - 确保总结准确反映原文主旨
            - 使用简洁、专业的语言

            ## 输出格式
            - 根据内容复杂度选择合适的总结深度
            - 简单内容：一段话总结
            - 复杂内容：分层总结（概要 + 要点列表）
            - 关键数据和结论必须保留
            """;

    public SummaryAgent(List<ToolCallback> tools, List<Advisor> advisors) {
        super("SummaryAgent", "专注于内容总结、提炼要点、生成摘要", tools, advisors);
    }

    @Override
    public String type() {
        return "result";
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String call(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext).call().content();
    }

    @Override
    public Flux<String> stream(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext).stream().content();
    }
}
