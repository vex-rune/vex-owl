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
 * AI事件订阅
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class SseApi {

    private final SseEventBroadcaster broadcaster;

    /**
     * 事件-订阅指定会话
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> events(@RequestParam String userId) {
        return broadcaster.subscribe(userId)
                .doOnCancel(() -> broadcaster.unsubscribe(userId))
                .doOnTerminate(() -> broadcaster.unsubscribe(userId));
    }
}
