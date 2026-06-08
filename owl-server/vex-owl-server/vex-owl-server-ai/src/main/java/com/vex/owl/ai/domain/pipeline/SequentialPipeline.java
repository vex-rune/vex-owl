package com.vex.owl.ai.domain.pipeline;

import com.vex.owl.ai.domain.AiManager;
import com.vex.owl.ai.domain.agent.Agent;
import com.vex.owl.ai.domain.agent.AgentManager;
import com.vex.owl.ai.domain.agent.SequentialPipelineAgent;
import com.vex.owl.ai.domain.agent.SequentialPipelineAgent.PipelineStep;
import com.vex.owl.ai.domain.agent.SequentialPipelineAgent.PlanStep;
import com.vex.owl.ai.domain.agent.SummaryAgent;
import com.vex.owl.ai.domain.context.RunContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 顺序管道
 *
 * <p>LLM 动态规划执行顺序，然后按顺序执行每个 Agent，上一个的输出作为下一个的输入</p>
 */
@Getter
@Builder
@Slf4j
@Component
public class SequentialPipeline implements Pipeline {

    private final AgentManager agentManager;
    private final AiManager aiManager;
    private final SequentialPipelineAgent sequentialPipelineAgent;

    @Override
    public Result execute(String input, RunContext context) {
        log.info("SequentialPipeline 开始编排");

        ChatClient client = aiManager.createClient(context);

        List<Object> resultList = new ArrayList<>();

        PipelineStep pipelineStep =
                sequentialPipelineAgent.call(input, client, context);

        log.debug("SequentialPipeline 执行计划: {}", pipelineStep);

        List<PlanStep> steps = pipelineStep.steps();

        resultList.add(pipelineStep.content());

        for (PlanStep step : steps) {
            Agent<?> agent = agentManager.getAgent(step.agentName()).orElse(null);
            if (agent == null) {
                log.warn("SequentialPipeline Agent[{}] 未找到，跳过", step.agentName());
                continue;
            }

            RunContext stepContext = context.addStep().withResult(pipelineStep.content());
            Object object = agent.call(step.description(), client, stepContext);
            resultList.add(object);
        }

        log.info("SequentialPipeline 总结: {}", resultList);
        SummaryAgent summaryAgent = agentManager.getAgent(SummaryAgent.class)
                .orElseThrow(() -> new RuntimeException("SummaryAgent 未找到"));

        String call = summaryAgent.call(resultList.toString(), client, context);
        log.info("SequentialPipeline 总结: {}", call);

        return new Result(input, pipelineStep, resultList, call);
    }

    public record Result(
            String input,
            PipelineStep plan,
            List<Object> steps,
            String summary
    ) {}
}
