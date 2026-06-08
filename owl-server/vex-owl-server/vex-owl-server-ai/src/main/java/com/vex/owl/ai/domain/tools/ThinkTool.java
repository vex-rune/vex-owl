package com.vex.owl.ai.domain.tools;

import com.vex.owl.ai.domain.event.ChatContentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内置思考工具
 *
 * <p>所有从工厂生产的 ChatClient 天生携带此工具。
 * 模型在执行 Action 前必须先调用 think 记录思考过程，
 * 确保每一步推理都有可观测的思维链。</p>
 *
 * <p>思考记录通过 Spring 事件发布，由 SseEventBroadcaster 推送到前端。</p>
 */
@Slf4j
@Component
public class ThinkTool {

    private final ApplicationEventPublisher publisher;

    public ThinkTool(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Tool(description = """
            记录你的思考过程。在执行任何 Action 之前，你必须先调用此工具，
            清晰阐述：当前现状、面临的问题、下一步计划。
            返回值确认你的思考已记录。""")
    public String think(
            @ToolParam(description = "你的完整思考过程，包括现状分析、问题识别、下一步计划") String thought,
            ToolContext toolContext) {

        String tenantId = toolContextExtractor.getTenantId(toolContext).orElse("unknown");
        String sessionId = toolContextExtractor.getSessionId(toolContext).orElse("unknown");

        log.info("[Think] tenant={}, session={}\n{}", tenantId, sessionId, thought);

        publisher.publishEvent(ChatContentEvent.builder()
                .tenantId(tenantId)
                .sessionId(sessionId)
                .content(thought)
                .stream(false)
                .finish(false)
                .timestamp(System.currentTimeMillis())
                .build());

        return "思考已记录";
    }

    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();
}
