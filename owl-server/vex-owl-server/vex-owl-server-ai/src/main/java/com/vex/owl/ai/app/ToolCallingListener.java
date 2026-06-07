package com.vex.owl.ai.app;

import com.vex.owl.ai.domain.context.DefaultRunContext;
import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.event.ToolCallRequestEvent;
import com.vex.owl.ai.domain.event.ToolCallResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 工具调用监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallingListener implements ToolCallingManager {

    private final ToolCallingManager toolCallingManager;
    private final ApplicationEventPublisher publisher;

    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        return toolCallingManager.resolveToolDefinitions(chatOptions);
    }

    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        Optional<AssistantMessage> toolCallMsg = chatResponse.getResults().stream()
                .filter(g -> !g.getOutput().getToolCalls().isEmpty())
                .map(g -> (AssistantMessage) g.getOutput())
                .findFirst();

        if (toolCallMsg.isEmpty()) {
            throw new IllegalStateException("No tool call requested by the chat model");
        }

        AssistantMessage assistantMessage = toolCallMsg.get();
        Map<String, Object> context = buildContext(prompt, assistantMessage);

        // 发布工具调用请求事件
        for (AssistantMessage.ToolCall tc : assistantMessage.getToolCalls()) {
            ToolCallRequestEvent event = ToolCallRequestEvent.builder()
                    .tenantId(getString(context, "tenantId"))
                    .sessionId(getString(context, "sessionId"))
                    .eventType(ToolCallRequestEvent.EventType.BEFORE_EXECUTE)
                    .toolCallId(tc.id())
                    .toolName(tc.name())
                    .arguments(tc.arguments())
                    .build();
            publisher.publishEvent(event);
        }

        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);

        // 发布工具调用结果事件
        Optional<ToolResponseMessage.ToolResponse> optional = result.conversationHistory().stream()
                .filter(m -> MessageType.TOOL.equals(m.getMessageType()))
                .map(m -> (ToolResponseMessage) m)
                .filter(m -> assistantMessage.getToolCalls().stream()
                        .map(AssistantMessage.ToolCall::id)
                        .anyMatch(id -> m.getResponses().stream().anyMatch(r -> Objects.equals(id, r.id()))))
                .map(m -> m.getResponses().stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        if (optional.isPresent()) {
            RunContext runContext = RunContext.fromMap(context);
            ToolCallResultEvent event = new ToolCallResultEvent(
                    runContext.getTenantId(),
                    runContext.getSessionId(),
                    ToolCallRequestEvent.EventType.AFTER_EXECUTE,
                    optional.get());
            publisher.publishEvent(event);
        }

        return result;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Map<String, Object> buildContext(Prompt prompt, AssistantMessage assistantMessage) {
        Map<String, Object> context = new HashMap<>();

        if (prompt.getOptions() instanceof ToolCallingChatOptions options) {
            Map<String, Object> toolContext = options.getToolContext();
            if (toolContext != null) {
                context.putAll(toolContext);
            }
        }

        return context;
    }
}
