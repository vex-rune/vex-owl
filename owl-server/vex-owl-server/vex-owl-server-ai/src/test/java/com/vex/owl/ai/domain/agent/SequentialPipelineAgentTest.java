package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.tools.AvailableAgentsTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SequentialPipelineAgentTest {

    @Test
    void sequentialPipelineAgent_shouldHaveCorrectType() {
        SequentialPipelineAgent agent = new SequentialPipelineAgent(null);

        assertThat(agent.type()).isEqualTo("begin");
        assertThat(agent.getDefinition().name()).isEqualTo("SequentialPipelineAgent");
        assertThat(agent.getDefinition().type()).isEqualTo("begin");
    }

    @Test
    void sequentialPipelineAgent_shouldNotDependOnAgentManager() {
        // 验证构造器只接受 ObjectProvider<AvailableAgentsTool>，不接受 AgentManager
        ObjectProvider<AvailableAgentsTool> provider = null;
        SequentialPipelineAgent agent = new SequentialPipelineAgent(provider);

        assertThat(agent).isNotNull();
    }

    @Test
    void pipelinePlanner_planPipeline_shouldStoreSteps() {
        SequentialPipelineAgent.PipelinePlanner planner = new SequentialPipelineAgent.PipelinePlanner();

        List<SequentialPipelineAgent.PlanStep> steps = List.of(
                new SequentialPipelineAgent.PlanStep("SimpleAgent", "翻译成英文"),
                new SequentialPipelineAgent.PlanStep("SummaryAgent", "总结要点")
        );

        String result = planner.planPipeline(steps);

        assertThat(result).contains("2");
        assertThat(planner.getSteps()).hasSize(2);
        assertThat(planner.getSteps().get(0).agentName()).isEqualTo("SimpleAgent");
        assertThat(planner.getSteps().get(0).description()).isEqualTo("翻译成英文");
    }

    @Test
    void pipelinePlanner_planPipeline_shouldReturnConfirmationMessage() {
        SequentialPipelineAgent.PipelinePlanner planner = new SequentialPipelineAgent.PipelinePlanner();

        List<SequentialPipelineAgent.PlanStep> steps = List.of(
                new SequentialPipelineAgent.PlanStep("SimpleAgent", "任务A")
        );

        String result = planner.planPipeline(steps);

        assertThat(result).isEqualTo("规划完成，共 1 个步骤");
    }

    @Test
    void planStep_shouldBeRecord() {
        SequentialPipelineAgent.PlanStep step =
                new SequentialPipelineAgent.PlanStep("Agent", "desc");

        assertThat(step.agentName()).isEqualTo("Agent");
        assertThat(step.description()).isEqualTo("desc");
    }

    @Test
    void pipelineStep_shouldContainStepsAndContent() {
        List<SequentialPipelineAgent.PlanStep> steps = List.of(
                new SequentialPipelineAgent.PlanStep("A", "任务A")
        );

        SequentialPipelineAgent.PipelineStep pipelineStep =
                new SequentialPipelineAgent.PipelineStep("思路内容", steps);

        assertThat(pipelineStep.content()).isEqualTo("思路内容");
        assertThat(pipelineStep.steps()).hasSize(1);
    }

    @Test
    void pipelineStep_shouldHandleNullSteps() {
        SequentialPipelineAgent.PipelineStep pipelineStep =
                new SequentialPipelineAgent.PipelineStep("简单任务", null);

        assertThat(pipelineStep.content()).isEqualTo("简单任务");
        assertThat(pipelineStep.steps()).isNull();
    }
}
