package com.vex.owl.ai.domain.agent;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent 注册表
 *
 * <p>持有所有 Agent 实例的只读视图。
 * 作为共享数据源，供 AgentManager 和 Tool 等组件直接读取，消除循环依赖。</p>
 */
@Component
public class AgentRegistry {

    private final List<Agent> agents;

    public AgentRegistry(List<Agent> agents) {
        this.agents = List.copyOf(agents);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<AgentDefinition> getAgentDefinitions() {
        return agents.stream().map(Agent::getDefinition).toList();
    }
}
