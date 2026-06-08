package com.vex.owl.ai.domain.tools;

import com.vex.owl.ai.domain.event.ToolCallRequestEvent;
import com.vex.owl.ai.domain.event.ToolCallResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 基于事件发布的工具调用管理器
 *
 * <p>实现 {@link ToolCallingManager} 接口，内部委托 {@link DefaultToolCallingManager}，
 * 在工具执行前后通过 Spring 事件机制发布输入/输出数据。</p>
 */
@Slf4j
public class EventPublishingToolCallingManager implements ToolCallingManager {

    private final DefaultToolCallingManager delegate;
    private final ApplicationEventPublisher publisher;

    public EventPublishingToolCallingManager(DefaultToolCallingManager delegate,
                                             ApplicationEventPublisher publisher) {
        this.delegate = delegate;
        this.publisher = publisher;
    }

    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        return delegate.resolveToolDefinitions(chatOptions);
    }

    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        Map<String, Object> toolContext = extractToolContext(prompt);
        String tenantId = getString(toolContext, "tenantId");
        String sessionId = getString(toolContext, "sessionId");

        Optional<AssistantMessage> toolCallMsg = chatResponse.getResults().stream()
                .filter(g -> !g.getOutput().getToolCalls().isEmpty())
                .map(g -> (AssistantMessage) g.getOutput())
                .findFirst();

        if (toolCallMsg.isPresent()) {
            AssistantMessage assistantMessage = toolCallMsg.get();

            for (AssistantMessage.ToolCall tc : assistantMessage.getToolCalls()) {
                publisher.publishEvent(ToolCallRequestEvent.builder()
                        .tenantId(tenantId)
                        .sessionId(sessionId)
                        .eventType(ToolCallRequestEvent.EventType.BEFORE_EXECUTE)
                        .toolCallId(tc.id())
                        .toolName(tc.name())
                        .arguments(tc.arguments())
                        .build());
            }
        }

        ToolExecutionResult result = delegate.executeToolCalls(prompt, chatResponse);

        if (toolCallMsg.isPresent()) {
            AssistantMessage assistantMessage = toolCallMsg.get();

            result.conversationHistory().stream()
                    .filter(m -> m.getMessageType() == org.springframework.ai.chat.messages.MessageType.TOOL)
                    .map(m -> (ToolResponseMessage) m)
                    .filter(m -> assistantMessage.getToolCalls().stream()
                            .map(AssistantMessage.ToolCall::id)
                            .anyMatch(id -> m.getResponses().stream()
                                    .anyMatch(r -> Objects.equals(id, r.id()))))
                    .flatMap(m -> m.getResponses().stream())
                    .forEach(response -> publisher.publishEvent(ToolCallResultEvent.builder()
                            .tenantId(tenantId)
                            .sessionId(sessionId)
                            .eventType(ToolCallRequestEvent.EventType.AFTER_EXECUTE)
                            .toolCallId(response.id())
                            .toolName(response.name())
                            .result(response.responseData())
                            .build()));
        }

        return result;
    }

    private Map<String, Object> extractToolContext(Prompt prompt) {
        if (prompt.getOptions() instanceof ToolCallingChatOptions options) {
            Map<String, Object> ctx = options.getToolContext();
            return ctx != null ? ctx : Map.of();
        }
        return Map.of();
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}
