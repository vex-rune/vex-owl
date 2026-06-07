package com.vex.owl.ai.domain.pipeline;

import com.vex.owl.ai.domain.agent.Agent;
import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.agent.AgentManager;
import com.vex.owl.ai.domain.agent.AgentRequest;
import com.vex.owl.ai.domain.context.RunContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 顺序管道
 *
 * <p>LLM 动态规划执行顺序，然后按顺序执行每个 Agent，上一个的输出作为下一个的输入</p>
 *
 * <pre>
 * input → LLM 规划 [Agent1, Agent2, ...] → Agent1(input) → Agent2(output1) → ... → result
 * </pre>
 */
@Getter
@Builder
@Slf4j
public class SequentialPipeline implements Pipeline, Agent {

    private final ChatClient client;
    private final AgentManager agentManager;

    // ==================== Agent 接口 ====================

    @Override
    public AgentDefinition getDefinition() {
        return new AgentDefinition("SequentialPipeline", "顺序管道，LLM 动态规划执行顺序");
    }

    @Override
    public AgentRequest prompt(String input) {
        return new PipelineRequest(this, input);
    }

    // ==================== Pipeline 接口 ====================

    @Override
    public String execute(String input, RunContext context) {
        log.info("SequentialPipeline 开始编排");

        // 1. LLM 规划
        PipelinePlanner planner = new PipelinePlanner();
        var response = client.prompt()
                .system(buildSystemPrompt())
                .user(input)
                .toolCallbacks(ToolCallbacks.from(planner))
                .call();

        log.info("SequentialPipeline LLM 响应: {}", response.chatResponse() != null
                ? response.chatResponse().getResult().getOutput().getText()
                : "null");

        List<PlanStep> steps = planner.getSteps();
        if (steps == null || steps.isEmpty()) {
            log.warn("SequentialPipeline LLM 未返回执行计划");
            return context.getPreviousResult();
        }

        log.info("SequentialPipeline 执行计划: {}",
                steps.stream().map(PlanStep::agentName).collect(Collectors.joining(" → ")));

        // 2. 顺序执行
        String currentInput = input;
        for (int i = 0; i < steps.size(); i++) {
            PlanStep step = steps.get(i);
            Agent agent = agentManager.getAgent(step.agentName()).orElse(null);
            if (agent == null) {
                log.warn("SequentialPipeline Agent[{}] 未找到，跳过", step.agentName());
                continue;
            }

            log.debug("[{}/{}] {} - {}", i + 1, steps.size(), step.agentName(), step.description());
            RunContext stepContext = context.withStep(i + 1).withResult(currentInput);
            currentInput = agent.prompt(currentInput).call(stepContext);
        }

        log.info("SequentialPipeline 执行完成");
        return currentInput;
    }

    /**
     * 流式执行管道，规划阶段实时推送 chunk，执行阶段串行处理
     */
    public Flux<String> stream(String input, RunContext context) {
        log.info("SequentialPipeline 开始编排 (stream)");

        // 1. LLM 规划（流式，chunk 实时推送）
        PipelinePlanner planner = new PipelinePlanner();
        return client.prompt()
                .system(buildSystemPrompt())
                .user(input)
                .toolCallbacks(ToolCallbacks.from(planner))
                .stream()
                .content()
                // 规划阶段的 chunk 直接推送给客户端
                .concatWith(Mono.fromCallable(() -> {
                    List<PlanStep> steps = planner.getSteps();
                    if (steps == null || steps.isEmpty()) {
                        log.warn("SequentialPipeline LLM 未返回执行计划");
                        return context.getPreviousResult() != null ? context.getPreviousResult() : "";
                    }

                    log.info("SequentialPipeline 执行计划: {}",
                            steps.stream().map(PlanStep::agentName).collect(Collectors.joining(" → ")));

                    // 2. 顺序执行各步骤（每步内部流式收集后串行传递）
                    return executeSteps(input, steps, context);
                }).subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                  .flatMapMany(Flux::just));
    }

    /**
     * 按顺序执行各步骤，每步流式收集完整结果后传给下一步
     */
    private String executeSteps(String input, List<PlanStep> steps, RunContext context) {
        String currentInput = input;
        for (int i = 0; i < steps.size(); i++) {
            PlanStep step = steps.get(i);
            Agent agent = agentManager.getAgent(step.agentName()).orElse(null);
            if (agent == null) {
                log.warn("SequentialPipeline Agent[{}] 未找到，跳过", step.agentName());
                continue;
            }

            log.debug("[{}/{}] {} - {}", i + 1, steps.size(), step.agentName(), step.description());
            RunContext stepContext = context.withStep(i + 1).withResult(currentInput);

            String finalInput = currentInput;
            currentInput = agent.prompt(finalInput).stream(stepContext)
                    .collectList()
                    .map(chunks -> String.join("", chunks))
                    .block();
        }

        log.info("SequentialPipeline 执行完成");
        return currentInput;
    }

    // ==================== 内部 ====================

    private String buildSystemPrompt() {
        String agents = agentManager.getAvailableAgents().stream()
                .map(a -> "- " + a.name() + ": " + a.description())
                .collect(Collectors.joining("\n"));

        return """
                你是一个任务编排助手。你的唯一职责是分析用户输入，规划执行步骤，然后调用 planPipeline 工具。

                你只能调用 planPipeline 工具，不能调用其他任何工具。

                可用的 Agent（仅用于 planPipeline 的 agentName 参数，不是工具）:
                %s

                【重要规则】
                1. 你必须调用 planPipeline 工具，不要直接回复用户
                2. 每次收到用户输入后，都必须调用 planPipeline 传入执行步骤
                3. 即使只需要一个步骤，也要调用 planPipeline
                4. agentName 必须与上面列表中的名称完全一致
                5. 除了 planPipeline 之外，不要调用任何其他工具
                """.formatted(agents);
    }

    /**
     * 管道规划工具 —— LLM 通过调用此工具提交执行计划
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

    /**
     * 执行步骤
     */
    public record PlanStep(
            @ToolParam(description = "Agent 名称") String agentName,
            @ToolParam(description = "任务描述") String description
    ) {}

    /**
     * 管道请求构建器
     */
    private record PipelineRequest(SequentialPipeline pipeline, String input) implements AgentRequest {

        @Override
        public String call(RunContext runContext) {
            return pipeline.execute(input, runContext);
        }

        @Override
        public Flux<String> stream(RunContext runContext) {
            return Flux.just(pipeline.execute(input, runContext));
        }
    }
}
