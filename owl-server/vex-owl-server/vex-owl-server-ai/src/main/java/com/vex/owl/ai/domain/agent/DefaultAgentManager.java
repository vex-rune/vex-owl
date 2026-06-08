package com.vex.owl.ai.domain.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认 Agent 管理器实现
 */
@Component
@RequiredArgsConstructor
public class DefaultAgentManager implements AgentManager {

    private final AgentRegistry agentRegistry;

    @Override
    public List<AgentDefinition> getAvailableAgents() {
        return agentRegistry.getAgentDefinitions();
    }

    @Override
    public Optional<Agent> getAgent(String name) {
        return agentRegistry.getAgents().stream()
                .filter(agent -> Objects.equals(agent.getDefinition().name(), name))
                .findFirst();
    }

    @Override
    public <T extends Agent> Optional<T> getAgent(Class<T> type) {
        return agentRegistry.getAgents().stream().filter(type::isInstance).map(type::cast).findFirst();
    }
}
