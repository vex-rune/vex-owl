package com.vex.owl.ai.domain;

import com.vex.owl.ai.domain.agent.Agent;
import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.agent.AgentManager;
import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import com.vex.owl.ai.domain.tools.AgentAdvisor;
import com.vex.owl.ai.domain.tools.ThinkTool;
import com.vex.owl.ai.domain.tools.ToolDefinition;
import com.vex.owl.ai.domain.tools.ToolServer;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DefaultAiManager implements AiManager {

    private final AgentManager agentManager;
    private final ToolServer toolServer;
    private final ModelProductFactory modelProductFactory;
    private final List<AgentAdvisor> agentAdvisors;
    private final ThinkTool thinkTool;

    @Override
    public List<AgentDefinition> getAgents(String userId) {
        return agentManager.getAvailableAgents();
    }

    @Override
    public Optional<Agent> getAgent(String userId, String name) {
        return agentManager.getAgent(name);
    }

    @Override
    public List<ToolDefinition> getTools(String userId) {
        return toolServer.getAllTools().stream()
                .map(tool -> ToolDefinition.of(tool.getName(), tool.getClass().getSimpleName()))
                .toList();
    }

    @Override
    public ChatClient createClient(RunContext runContext) {
        List<Advisor> advisors = agentAdvisors.stream()
                .map(AgentAdvisor::toAdvisor)
                .collect(Collectors.toList());

        List<ToolCallback> builtInTools = List.of(ToolCallbacks.from(thinkTool));

        return modelProductFactory.createClient(
                        runContext.getModelProperties().getProviderCode(),
                        runContext.getModelProperties())
                .mutate()
                .defaultToolContext(Map.of(
                        "userId", runContext.getUserId(),
                        "sessionId", runContext.getSessionId() != null ? runContext.getSessionId() : ""))
                .defaultAdvisors(advisors)
                .defaultToolCallbacks(builtInTools)
                .build();
    }

    @Override
    public List<AgentAdvisor> getAdvisors(String userId) {
        return agentAdvisors;
    }

    @Override
    public AiCapability getCapabilities(String userId) {
        return new AiCapability(userId, getAgents(userId), getTools(userId));
    }
}
