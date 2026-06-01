package com.vex.owl.ai.domain.event;

import com.vex.owl.ai.domain.event.ToolCallRequestEvent.EventType;
import com.vex.owl.ai.domain.event.ToolCallRequestEvent.ToolCallInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallingListener implements ToolCallingManager {

    final ToolCallingManager toolCallingManager;
    final ApplicationEventPublisher publisher;

    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        log.trace("start resolveToolDefinitions:{}", chatOptions);
        List<ToolDefinition> toolDefinitions = toolCallingManager.resolveToolDefinitions(chatOptions);
        log.trace("end resolveToolDefinitions:{}", chatOptions);
        return toolDefinitions;
    }

    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {


        Optional<Generation> toolCallGeneration = chatResponse.getResults()
                .stream()
                .filter(g -> !CollectionUtils.isEmpty(g.getOutput().getToolCalls()))
                .findFirst();

        if (toolCallGeneration.isEmpty()) {
            throw new IllegalStateException("No tool call requested by the chat model");
        }

        AssistantMessage assistantMessage = toolCallGeneration.get().getOutput();

        ToolContext toolContext = buildToolContext(prompt, assistantMessage);

        log.info("tool call:{}", assistantMessage.getToolCalls());

        List<ToolCallInfo> toolCallInfos = assistantMessage.getToolCalls().stream()
                .map(tc -> new ToolCallInfo(tc.id(), tc.name(), tc.arguments()))
                .toList();

        for (ToolCallInfo toolCallInfo : toolCallInfos) {
            publisher.publishEvent(new ToolCallRequestEvent(
                    toolContext.getContext(),
                    EventType.BEFORE_EXECUTE,
                    toolCallInfo
            ));
        }


        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);


        List<ToolResponseMessage.ToolResponse> list = result.conversationHistory().stream().filter(
                        message -> MessageType.TOOL.equals(message.getMessageType())
                ).filter(message -> message instanceof ToolResponseMessage)
                .map(message -> ((ToolResponseMessage) message))
                .map(message -> message.getResponses().stream()
                        .filter(response -> assistantMessage.getToolCalls().stream().map(AssistantMessage.ToolCall::id).toList().contains(response.id()))
                        .toList()
                ).flatMap(List::stream).toList();

        log.info("tool call result:{}", list);

        for (ToolResponseMessage.ToolResponse toolResponses : list) {
            publisher.publishEvent(new ToolCallResultEvent(
                    toolContext.getContext(),
                    EventType.AFTER_EXECUTE,
                    toolResponses
            ));
        }


        return result;
    }


    private static ToolContext buildToolContext(Prompt prompt, AssistantMessage assistantMessage) {
        Map<String, Object> toolContextMap = Map.of();

        if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions
                && !CollectionUtils.isEmpty(toolCallingChatOptions.getToolContext())) {
            toolContextMap = new HashMap<>(toolCallingChatOptions.getToolContext());

            toolContextMap.put(ToolContext.TOOL_CALL_HISTORY,
                    buildConversationHistoryBeforeToolExecution(prompt, assistantMessage));
        }

        return new ToolContext(toolContextMap);
    }


    private static List<Message> buildConversationHistoryBeforeToolExecution(Prompt prompt,
                                                                             AssistantMessage assistantMessage) {
        List<Message> messageHistory = new ArrayList<>(prompt.copy().getInstructions());
        messageHistory.add(AssistantMessage.builder()
                .content(assistantMessage.getText())
                .properties(assistantMessage.getMetadata())
                .toolCalls(assistantMessage.getToolCalls())
                .build());
        return messageHistory;
    }
}
