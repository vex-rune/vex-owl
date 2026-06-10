package com.vex.owl.ai.domain;

import com.vex.owl.ai.domain.agent.Agent;
import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.tools.AgentAdvisor;
import com.vex.owl.ai.domain.tools.ToolDefinition;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.Optional;

/**
 * AI 管理器接口
 *
 * <p>统一管理 Agent、Tool、ChatClient 创建，按租户隔离资源。</p>
 */
public interface AiManager {

    // === Agent ===

    List<AgentDefinition> getAgents(String userId);

    Optional<Agent> getAgent(String userId, String name);

    // === Tool ===

    List<ToolDefinition> getTools(String userId);

    // === ChatClient ===

    /**
     * 根据 RunContext 创建 ChatClient
     */
    ChatClient createClient(RunContext runContext);

    // === Advisor ===

    List<AgentAdvisor> getAdvisors(String userId);

    // === 汇总 ===

    AiCapability getCapabilities(String userId);

    record AiCapability(
            String userId,
            List<AgentDefinition> agents,
            List<ToolDefinition> tools
    ) {}
}
