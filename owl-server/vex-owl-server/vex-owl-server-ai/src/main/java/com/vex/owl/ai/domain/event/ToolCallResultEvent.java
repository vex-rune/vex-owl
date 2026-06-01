package com.vex.owl.ai.domain.tools.event;

import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

public record ToolCallResultEvent(Map<String, Object> context, ToolCallRequestEvent.EventType eventType,
                                  ToolResponseMessage.ToolResponse toolResponses) {
}
