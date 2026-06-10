package com.vex.owl.ai.domain.tools;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.event.ChatContentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 内置思考工具
 *
 * <p>所有从工厂生产的 ChatClient 天生携带此工具。
 * 模型在执行 Action 前必须先调用 think 记录思考过程，
 * 确保每一步推理都有可观测的思维链。</p>
 *
 * <p>思考记录通过 EventPublisher 发布，由 SseEventBroadcaster 推送到前端。</p>
 */
@Slf4j
@Component
public class ThinkTool {

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION_ID = "sessionId";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_MODEL = "model";

    private final EventPublisher eventPublisher;

    public ThinkTool(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Tool(description = """
            记录你的思考过程。在执行任何 Action 之前，你必须先调用此工具，
            清晰阐述：当前现状、面临的问题、下一步计划。
            返回值确认你的思考已记录。""")
    public String think(
            @ToolParam(description = "你的完整思考过程，包括现状分析、问题识别、下一步计划") String thought,
            ToolContext toolContext) {

        log.debug("[Think] {}", thought);

        Map<String, Object> ctx = toolContext != null ? toolContext.getContext() : Map.of();

        eventPublisher.publish("ChatContentEvent", ChatContentEvent.builder()
                .userId(getString(ctx, KEY_USER_ID))
                .sessionId(getString(ctx, KEY_SESSION_ID))
                .provider(getString(ctx, KEY_PROVIDER))
                .modelName(getString(ctx, KEY_MODEL))
                .content(thought)
                .stream(false)
                .finish(false)
                .timestamp(System.currentTimeMillis())
                .build());

        return "思考已记录";
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}