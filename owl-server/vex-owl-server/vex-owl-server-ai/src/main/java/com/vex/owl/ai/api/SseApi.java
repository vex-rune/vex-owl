package com.vex.owl.ai.api;

import com.vex.owl.ai.app.agent.SseEventBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * SSE 事件订阅端点
 *
 * <p>客户端通过此端点订阅实时事件（工具调用、Token 使用量等）</p>
 *
 * <p>用法：GET /api/ai/events?sessionId=xxx</p>
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SseApi {

    private final SseEventBroadcaster broadcaster;

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> events(@RequestParam String sessionId) {
        return broadcaster.subscribe(sessionId)
                .doOnCancel(() -> broadcaster.unsubscribe(sessionId))
                .doOnTerminate(() -> broadcaster.unsubscribe(sessionId));
    }
}
