package com.vex.owl.ai.domain.tools;

import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.agent.AgentRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 查询可用 Agent 工具
 *
 * <p>供 LLM 在任务编排前调用，返回当前租户有权限访问的 AgentDef 列表。
 * 租户隔离通过 ToolContext 中的 tenantId 实现，不由大模型提供。</p>
 *
 * <p>直接依赖 {@link AgentRegistry}，无懒加载，无循环依赖。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AvailableAgentsTool {

    private final AgentRegistry agentRegistry;
    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();

    @Tool(name = "availableAgents", description = "查询当前可用的 Agent 列表。在进行任务编排前必须先调用此工具，获取可调度的 Agent 名称和能力描述。")
    public String getAvailableAgents(ToolContext toolContext) {
        String tenantId = toolContextExtractor.getTenantId(toolContext).orElse("unknown");
        log.info("查询可用 Agent, tenantId={}", tenantId);

        List<AgentDefinition> agents = agentRegistry.getAgentDefinitions();

        if (agents.isEmpty()) {
            return "当前没有可用的 Agent";
        }

        String result = agents.stream()
                .map(a -> "- " + a.name() + " (" + a.type() + "): " + a.description())
                .collect(Collectors.joining("\n"));

        log.info("返回 {} 个 Agent, tenantId={}", agents.size(), tenantId);
        return result;
    }
}
