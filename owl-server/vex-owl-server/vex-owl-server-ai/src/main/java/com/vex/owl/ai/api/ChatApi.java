package com.vex.owl.ai.api;

import java.util.List;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.app.chat.ChatApp;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatMessageEntity;
import com.vex.owl.ai.domain.chat.ChatSessionEntity;
import com.vex.security.auth.AuthHeaderConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatApi {
    private final ChatApp chatApp;
    private final ChatManager chatManager;

    /// sse
    @PostMapping(value = "/free", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
                             @RequestBody FreeChatMessageRequest request) {
        return chatApp.chat(userId, request.getPrompt())
                .onErrorResume(e -> Flux.just("系统错误：" + e.getMessage()));
    }

    @GetMapping("session/list")
    public ApiResponse<List<ChatSessionEntity>> sessionList(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatSessionEntity> sessions = chatManager.getSessions(userId, page, size);
        return ApiResponse.success(sessions.getContent());
    }

    @GetMapping("session/{sessionId}/messages")
    public ApiResponse<List<ChatMessageEntity>> sessionMessages(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String sessionId) {
        List<ChatMessageEntity> messages = chatManager.getMessagesAsc(sessionId);
        return ApiResponse.success(messages);
    }
}
