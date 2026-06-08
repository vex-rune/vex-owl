package com.vex.owl.ai.api;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.AiManager;
import com.vex.owl.ai.domain.agent.AgentDefinition;
import com.vex.owl.ai.domain.tools.ToolDefinition;
import com.vex.security.auth.AuthHeaderConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 管理 API
 *
 * <p>提供 Agent 和 Tool 的统一查询接口，按租户隔离。</p>
 */
@RestController
@RequestMapping("/api/ai/manager")
@RequiredArgsConstructor
public class AiManagerApi {

    private final AiManager aiManager;

    /**
     * 获取当前租户下所有可用的 Agent
     */
    @GetMapping("/agents")
    public ApiResponse<List<AgentDefinition>> getAgents(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getAgents(userId));
    }

    /**
     * 根据名称查询指定 Agent
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
     * 获取当前租户下所有可用的 Tool
     */
    @GetMapping("/tools")
    public ApiResponse<List<ToolDefinition>> getTools(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getTools(userId));
    }

    /**
     * 获取当前租户下所有可用资源汇总（Agent + Tool）
     */
    @GetMapping("/capabilities")
    public ApiResponse<AiManager.AiCapability> getCapabilities(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId) {
        return ApiResponse.success(aiManager.getCapabilities(userId));
    }
}
