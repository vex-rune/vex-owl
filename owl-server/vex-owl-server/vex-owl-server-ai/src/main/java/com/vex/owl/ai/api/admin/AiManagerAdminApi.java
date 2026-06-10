package com.vex.owl.ai.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.AiManager;
import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.tools.ToolDefinition;
import com.vex.security.auth.AuthHeaderConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 管理-管理员
 *
 * <p>提供 Agent 和 Tool 的统一查询接口，按租户隔离。</p>
 */
@RestController
@RequestMapping("/api/ai/admin/manager")
@RequiredArgsConstructor
public class AiManagerAdminApi {

    private final AiManager aiManager;

    /**
     * Agent-查询所有
     */
    @GetMapping("/agents")
    public ApiResponse<List<AgentDefinition>> getAgents(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getAgents(userId));
    }

    /**
     * Agent-查询指定 Agent
     */
    @GetMapping("/agents/{name}")
    public ApiResponse<AgentDefinition> getAgent(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String name) {
        return aiManager.getAgent(userId, name)
                .map(agent -> ApiResponse.success(agent.getDefinition()))
                .orElse(ApiResponse.error("AGENT_NOT_FOUND", null, "Agent 不存在: " + name));
    }

    /**
     * Tool-查询所有
     */
    @GetMapping("/tools")
    public ApiResponse<List<ToolDefinition>> getTools(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getTools(userId));
    }

    /**
     * 资源-查询汇总（Agent + Tool）
     */
    @GetMapping("/capabilities")
    public ApiResponse<AiManager.AiCapability> getCapabilities(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getCapabilities(userId));
    }
}
