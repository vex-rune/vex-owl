package com.vex.owl.ai.domain.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAgentManagerTest {

    private DefaultAgentManager agentManager;

    @BeforeEach
    void setUp() {
        Agent simpleAgent = new SimpleAgent(List.of(), List.of());
        Agent summaryAgent = new SummaryAgent(List.of(), List.of());
        AgentRegistry registry = new AgentRegistry(List.of(simpleAgent, summaryAgent));
        agentManager = new DefaultAgentManager(registry);
    }

    @Test
    void getAgent_shouldReturnByName() {
        var result = agentManager.getAgent("SimpleAgent");

        assertThat(result).isPresent();
        assertThat(result.get().getDefinition().name()).isEqualTo("SimpleAgent");
    }

    @Test
    void getAgent_shouldReturnEmptyForUnknown() {
        var result = agentManager.getAgent("NonExistentAgent");

        assertThat(result).isEmpty();
    }

    @Test
    void getAgent_byClass_shouldReturnAgent() {
        var result = agentManager.getAgent(SimpleAgent.class);

        assertThat(result).isPresent();
        assertThat(result.get()).isInstanceOf(SimpleAgent.class);
    }

    @Test
    void getAvailableAgents_shouldReturnAllDefinitions() {
        List<AgentDefinition> agents = agentManager.getAvailableAgents();

        assertThat(agents).hasSize(2);
        assertThat(agents).extracting(AgentDefinition::name)
                .containsExactlyInAnyOrder("SimpleAgent", "SummaryAgent");
    }

    @Test
    void getAvailableAgents_shouldReturnCorrectTypes() {
        List<AgentDefinition> agents = agentManager.getAvailableAgents();

        Map<String, String> typeMap = agents.stream()
                .collect(java.util.stream.Collectors.toMap(
                        AgentDefinition::name, AgentDefinition::type));

        assertThat(typeMap.get("SimpleAgent")).isEqualTo("node");
        assertThat(typeMap.get("SummaryAgent")).isEqualTo("result");
    }
}
