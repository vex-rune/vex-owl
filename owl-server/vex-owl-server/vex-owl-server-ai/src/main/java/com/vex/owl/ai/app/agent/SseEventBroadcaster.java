package com.vex.owl.ai.app.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vex.event.Event;
import com.vex.owl.ai.domain.event.*;
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
 * <p>订阅 Spring 事件，按 userId 路由到对应的 SSE 流。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SseEventBroadcaster {

    private final ObjectMapper objectMapper;

    private final Map<String, Sinks.Many<String>> sessions = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> sessionRefs = new ConcurrentHashMap<>();

    public Flux<String> subscribe(String userId) {
        Sinks.Many<String> sink = sessions.computeIfAbsent(userId,
                k -> Sinks.many().multicast().onBackpressureBuffer());
        sessionRefs.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
        return sink.asFlux();
    }

    public void unsubscribe(String userId) {
        AtomicInteger ref = sessionRefs.get(userId);
        if (ref != null && ref.decrementAndGet() <= 0) {
            sessions.remove(userId);
            sessionRefs.remove(userId);
        }
    }

    @EventListener
    public void onEvent(Event event) {
        String userId = event.getMetadata().userId();
        if (userId == null || userId.isEmpty()) return;

        String eventType = event.getMetadata().eventType();
        Object payload = event.getPayload();

        if (payload instanceof ToolCallRequestEvent
                || payload instanceof ToolCallResultEvent
                || payload instanceof TokenUsageEvent
                || payload instanceof ChatContentEvent) {
            broadcast(userId, eventType, payload);
        }
    }

    private void broadcast(String userId, String eventType, Object data) {
        Sinks.Many<String> sink = sessions.get(userId);
        if (sink == null) return;

        try {
            Map<String, Object> envelope = Map.of(
                    "event", eventType,
                    "data", data,
                    "timestamp", System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(envelope);
            sink.tryEmitNext(json);
        } catch (Exception e) {
            log.warn("广播事件失败: userId={}, event={}", userId, eventType, e);
        }
    }
}
