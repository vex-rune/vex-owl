package com.vex.owl.ai.domain.agent;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Agent {

    String process(String input, AgentContext agentContext);

    String getName();


    @AllArgsConstructor
    public static class SimplAgent implements Agent {
        private final ChatClient client;
        private final List<ToolCallback> tools;
        private final AgentContext context;

        public String process(String input, AgentContext agentContext) {

            Map<String, Object> map = agentContext.toMap();

            return client.prompt(input)
                    .toolContext(map)
                    .toolCallbacks(tools)
                    .call()
                    .content();
        }

        @Override
        public String getName() {
            return "SimpleAgent"; // Changed from "SimpleAgent" to "simpleAgent"
        }


    }

    /**
     * Agent 执行上下文
     *
     * @param tenantId       租户ID
     * @param sessionId      会话ID
     * @param traceId        追踪ID
     * @param step           步骤
     * @param previousResult 上一步结果
     * @param metadata       扩展元数据
     */
    public record AgentContext(
            String tenantId,        // 租户ID
            String sessionId,      // 会话ID
            String traceId,        // 追踪ID
            int step,              // 当前步骤
            String previousResult, // 上一步结果
            Map<String, Object> metadata  // 扩展元数据
    ) {

        /**
         * 创建新上下文
         */
        public static AgentContext of(String tenantId, String sessionId) {
            return new AgentContext(
                    tenantId,
                    sessionId,
                    UUID.randomUUID().toString(),
                    1,
                    null,
                    Map.of()
            );
        }


        /**
         * 转为Map
         */
        public Map<String, Object> toMap() {
            return new java.util.HashMap<>() {{
                put("tenantId", tenantId);
                put("sessionId", sessionId);
                put("traceId", traceId);
                put("step", step);
                put("previousResult", previousResult);
                put("metadata", metadata);
            }};
        }

        /**
         * map 转 AgentContext
         */
        public static AgentContext fromMap(Map<String, Object> map) {
            return new AgentContext(
                    (String) map.get("tenantId"),
                    (String) map.get("sessionId"),
                    (String) map.get("traceId"),
                    (Integer) map.get("step"),
                    (String) map.get("previousResult"),
                    (Map<String, Object>) map.get("metadata")
            );
        }
    }

}


