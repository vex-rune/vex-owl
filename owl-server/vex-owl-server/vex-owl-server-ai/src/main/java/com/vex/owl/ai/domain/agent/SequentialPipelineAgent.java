package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.tools.AvailableAgentsTool;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SequentialPipelineAgent implements Agent<SequentialPipelineAgent.PipelineStep> {

    private final ObjectProvider<AvailableAgentsTool> availableAgentsToolProvider;

    public SequentialPipelineAgent(ObjectProvider<AvailableAgentsTool> availableAgentsToolProvider) {
        this.availableAgentsToolProvider = availableAgentsToolProvider;
    }

    /**
     * 管道执行步骤结果
     */
    public record PipelineStep(
            @ToolParam(description = "思路") String content,
            @ToolParam(description = "任务") List<PlanStep> steps
    ) {}

    /**
     * 管道规划步骤
     */
    public record PlanStep(
            @ToolParam(description = "Agent 名称") String agentName,
            @ToolParam(description = "任务描述") String description
    ) {}

    /**
     * 管道规划工具
     */
    public static class PipelinePlanner {

        @Getter
        private List<PlanStep> steps;

        @Tool(name = "planPipeline", description = "规划管道执行顺序。你必须调用此工具来提交执行计划，不要跳过。传入 Agent 名称和任务描述列表。")
        public String planPipeline(
                @ToolParam(description = "编排好的执行步骤列表") List<PlanStep> steps) {
            this.steps = steps;
            return "规划完成，共 " + steps.size() + " 个步骤";
        }
    }

    private static final String SYSTEM_PROMPT = """
            你是一个任务编排助手。你的职责是分析用户输入，判断任务复杂度，然后决定处理方式。

            ## 工作流程

            ### 第一步：查询可用 Agent
            你必须先调用 availableAgents 工具，获取当前可用的 Agent 列表。

            ### 第二步：评估任务复杂度
            根据以下标准判断任务级别：

            **简单任务**（无需编排，直接输出处理结果）：
            - 单一意图，无需多步骤处理
            - 例如：问候、简单问答、单句翻译、格式转换

            **复杂任务**（需要 Agent 编排）：
            - 包含多个子任务或需要多步骤处理
            - 例如：长文总结+翻译、数据分析+报告生成、多语言转换

            ### 第三步：执行
            - 如果是**简单任务**：直接输出处理结果，不要调用 planPipeline
            - 如果是**复杂任务**：调用 planPipeline 工具，传入编排好的执行步骤

            ## 规则
            1. agentName 必须与 availableAgents 返回的名称完全一致
            2. 除了 availableAgents 和 planPipeline 之外，不要调用任何其他工具
            """;

    @Override
    public AgentDefinition getDefinition() {
        return AgentDefinition.of(type(), "SequentialPipelineAgent", "任务编排助手");
    }

    @Override
    public String type() {
        return "begin";
    }

    @Override
    public PipelineStep call(String input, ChatClient client, RunContext runContext) {
        Map<String, Object> contextMap = runContext.toMap();
        PipelinePlanner pipelinePlanner = new PipelinePlanner();
        AvailableAgentsTool availableAgentsTool = availableAgentsToolProvider.getObject();

        client.prompt(input)
                .system(SYSTEM_PROMPT)
                .toolContext(contextMap)
                .advisors(s -> s.params(contextMap))
                .tools(availableAgentsTool, pipelinePlanner)
                .call()
                .content();

        List<PlanStep> steps = pipelinePlanner.getSteps();
        if (steps == null || steps.isEmpty()) {
            log.debug("任务评估为简单级别，跳过编排, userId={}", runContext.getUserId());
        } else {
            log.debug("任务编排完成，共 {} 个步骤, userId={}", steps.size(), runContext.getUserId());
        }

        return new PipelineStep(SYSTEM_PROMPT, steps);
    }

    @Override
    public Flux<PipelineStep> stream(String input, ChatClient client, RunContext runContext) {
        return Flux.just(call(input, client, runContext));
    }
}
