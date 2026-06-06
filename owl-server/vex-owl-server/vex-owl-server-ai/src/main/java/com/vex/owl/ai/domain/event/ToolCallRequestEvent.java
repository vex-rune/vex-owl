package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 工具调用请求事件
 *
 * <p>记录工具调用的请求信息，包括调用前后两个阶段</p>
 *
 * <h2>事件类型</h2>
 * <ul>
 *   <li>BEFORE_EXECUTE - 工具执行前</li>
 *   <li>AFTER_EXECUTE - 工具执行后</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 工具执行前
 * ToolCallRequestEvent beforeEvent = new ToolCallRequestEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("gpt-4")
 *         .aiType(AiContextMetadata.AiType.CHAT)
 *         .sessionId("session-123")
 *         .build(),
 *     ToolCallRequestEvent.EventType.BEFORE_EXECUTE,
 *     ToolCallInfo.builder()
 *         .toolCallId("call-001")
 *         .toolName("get_weather")
 *         .arguments("{\"city\": \"北京\"}")
 *         .build()
 * );
 *
 * // 工具执行后
 * ToolCallRequestEvent afterEvent = new ToolCallRequestEvent(
 *     metadata,
 *     ToolCallRequestEvent.EventType.AFTER_EXECUTE,
 *     toolInfo
 * );
 *
 * eventPublisher.publishEvent(afterEvent);
 * }</pre>
 *
 * @see AiUsageEvent
 * @see ToolCallInfo
 */
@Getter
public class ToolCallRequestEvent extends AiUsageEvent<ToolCallRequestEvent.ToolCallInfo> {

    /**
     * 事件类型
     */
    private final EventType eventType;

    /**
     * 构造函数
     *
     * @param metadata  AI 上下文元数据
     * @param eventType 事件类型
     * @param data      工具调用信息
     */
    public ToolCallRequestEvent(AiContextMetadata metadata,
                                EventType eventType,
                                ToolCallInfo data) {
        super(metadata, data, Instant.now());
        this.eventType = eventType;
    }

    /**
     * 构造函数（保留兼容性）
     *
     * @param toolContext 旧版 toolContext Map
     * @param eventType    事件类型
     * @param toolCalls   工具调用信息
     */
    @Deprecated
    public ToolCallRequestEvent(Map<String, Object> toolContext,
                                EventType eventType,
                                ToolCallInfo toolCalls) {
        super(
                AiContextMetadata.fromMap(toolContext),
                toolCalls,
                Instant.now()
        );
        this.eventType = eventType;
    }

    @Override
    public String getEventType() {
        return "TOOL_CALL_REQUEST";
    }

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 工具执行前
         */
        BEFORE_EXECUTE,

        /**
         * 工具执行后
         */
        AFTER_EXECUTE
    }

    /**
     * 工具调用信息
     */
    @Getter
    @Builder
    public static class ToolCallInfo {
        /**
         * 工具调用 ID
         */
        private String toolCallId;

        /**
         * 工具名称
         */
        private String toolName;

        /**
         * 工具调用参数（JSON 格式）
         */
        private String arguments;
    }
}
