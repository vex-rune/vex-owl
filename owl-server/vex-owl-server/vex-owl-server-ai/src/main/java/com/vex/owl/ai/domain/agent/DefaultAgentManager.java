package com.vex.owl.ai.domain.agent;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 默认 Agent 管理器实现
 */
@RequiredArgsConstructor
public class DefaultAgentManager implements AgentManager {

    private final List<Agent> agents;

    @Override
    public List<AgentDefinition> getAvailableAgents() {
        return agents.stream().map(Agent::getDefinition).toList();
    }

    @Override
    public Optional<Agent> getAgent(String name) {
        return agents.stream()
                .filter(agent -> Objects.equals(agent.getDefinition().name(), name))
                .findFirst();
    }

    @Override
    public <T extends Agent> Optional<T> getAgent(Class<T> type) {
        return agents.stream().filter(type::isInstance).map(type::cast).findFirst();
    }
}
