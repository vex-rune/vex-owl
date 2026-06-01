package com.vex.owl.ai.domain.event;

import lombok.Getter;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.time.Instant;
import java.util.Map;

/**
 * 工具调用结果事件
 *
 * <p>记录工具调用的结果信息</p>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * ToolCallResultEvent event = new ToolCallResultEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("gpt-4")
 *         .aiType(AiContextMetadata.AiType.CHAT)
 *         .sessionId("session-123")
 *         .build(),
 *     ToolCallRequestEvent.EventType.AFTER_EXECUTE,
 *     toolResponses
 * );
 *
 * eventPublisher.publishEvent(event);
 * }</pre>
 *
 * @see AiUsageEvent
 * @see ToolCallRequestEvent.EventType
 */
@Getter
public class ToolCallResultEvent extends AiUsageEvent<ToolResponseMessage.ToolResponse> {

    /**
     * 事件类型
     */
    private final ToolCallRequestEvent.EventType eventType;

    /**
     * 构造函数
     *
     * @param metadata    AI 上下文元数据
     * @param eventType   事件类型
     * @param data        工具响应结果
     */
    public ToolCallResultEvent(AiContextMetadata metadata,
                               ToolCallRequestEvent.EventType eventType,
                               ToolResponseMessage.ToolResponse data) {
        super(metadata, data, Instant.now());
        this.eventType = eventType;
    }

    /**
     * 构造函数（保留兼容性）
     *
     * @param context        旧版 context Map
     * @param eventType       事件类型
     * @param toolResponses   工具响应结果
     */
    @Deprecated
    public ToolCallResultEvent(Map<String, Object> context,
                               ToolCallRequestEvent.EventType eventType,
                               ToolResponseMessage.ToolResponse toolResponses) {
        super(
                AiContextMetadata.fromMap(context),
                toolResponses,
                Instant.now()
        );
        this.eventType = eventType;
    }

    @Override
    public String getEventType() {
        return "TOOL_CALL_RESULT";
    }
}
