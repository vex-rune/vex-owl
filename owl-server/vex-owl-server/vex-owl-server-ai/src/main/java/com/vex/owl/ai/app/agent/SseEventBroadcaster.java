package com.vex.owl.ai.app.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vex.owl.ai.domain.event.ChatContentEvent;
import com.vex.owl.ai.domain.event.TokenUsageEvent;
import com.vex.owl.ai.domain.event.ToolCallRequestEvent;
import com.vex.owl.ai.domain.event.ToolCallResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE 事件广播器
 *
 * <p>订阅 Spring 事件，按 sessionId 路由到对应的 SSE 流。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventBroadcaster {

    private final ObjectMapper objectMapper;

    private final Map<String, Sinks.Many<String>> sessions = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> sessionRefs = new ConcurrentHashMap<>();

    public Flux<String> subscribe(String sessionId) {
        Sinks.Many<String> sink = sessions.computeIfAbsent(sessionId,
                k -> Sinks.many().multicast().onBackpressureBuffer());
        sessionRefs.computeIfAbsent(sessionId, k -> new AtomicInteger(0)).incrementAndGet();
        return sink.asFlux();
    }

    public void unsubscribe(String sessionId) {
        AtomicInteger ref = sessionRefs.get(sessionId);
        if (ref != null && ref.decrementAndGet() <= 0) {
            sessions.remove(sessionId);
            sessionRefs.remove(sessionId);
        }
    }

    @EventListener
    public void onToolCallRequest(ToolCallRequestEvent event) {
        broadcast(event.getSessionId(), "tool_call_request", event);
    }

    @EventListener
    public void onToolCallResult(ToolCallResultEvent event) {
        broadcast(event.getSessionId(), "tool_call_result", event);
    }

    @EventListener
    public void onTokenUsage(TokenUsageEvent event) {
        broadcast(event.getSessionId(), "token_usage", event);
    }

    @EventListener
    public void onChatContent(ChatContentEvent event) {
        broadcast(event.getSessionId(), "chat_content", event);
    }

    private void broadcast(String sessionId, String eventType, Object data) {
        if (sessionId == null || sessionId.isEmpty()) return;

        Sinks.Many<String> sink = sessions.get(sessionId);
        if (sink == null) return;

        try {
            Map<String, Object> envelope = Map.of(
                    "event", eventType,
                    "data", data,
                    "timestamp", System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(envelope);
            sink.tryEmitNext(json);
        } catch (Exception e) {
            log.warn("广播事件失败: session={}, event={}", sessionId, eventType, e);
        }
    }
}
