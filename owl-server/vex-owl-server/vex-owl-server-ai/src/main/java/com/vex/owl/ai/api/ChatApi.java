package com.vex.owl.ai.api;

import java.util.List;
import java.util.Map;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.api.request.FreeChatMessageRequest;
import com.vex.owl.ai.api.request.PipelineRequest;
import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.app.chat.ChatApp;
import com.vex.owl.ai.app.chat.FreeModelPropertiesConfig;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatMessageEntity;
import com.vex.owl.ai.domain.chat.ChatSessionEntity;
import com.vex.owl.ai.domain.chat.UserMemoryEntity;
import com.vex.owl.ai.domain.chat.UserMemoryService;
import com.vex.owl.ai.domain.pipeline.SequentialPipeline;
import com.vex.security.auth.AuthHeaderConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * AI 对话业务接口
 *
 * <p>面向前端的对话相关接口，包括：对话、会话管理、消息历史、用户记忆。</p>
 */
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatApi {
    private final ChatApp chatApp;
    private final ChatManager chatManager;
    private final UserMemoryService userMemoryService;
    private final SequentialPipeline sequentialPipeline;
    private final FreeModelPropertiesConfig modelProperties;

    // ==================== 对话 ====================

    @PostMapping(value = "/free", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
                             @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                             @RequestBody FreeChatMessageRequest request) {
        return chatApp.chat(userId, sessionId, request.getPrompt())
                .onErrorResume(e -> Flux.just("系统错误：" + e.getMessage()));
    }

    /**
     * 顺序管道编排
     */
    @PostMapping(value = "/pipeline")
    public ApiResponse<SequentialPipeline.Result> pipeline(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
            @RequestBody @Valid PipelineRequest request) {

        RunContext context = RunContext.builder()
                .modelProperties(modelProperties)
                .tenantId(userId)
                .sessionId(sessionId)
                .headers(Map.of())
                .params(Map.of())
                .build();

        SequentialPipeline.Result result = sequentialPipeline.execute(request.getPrompt(), context);
        return ApiResponse.success(result);
    }

    // ==================== 会话管理 ====================

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionEntity>> sessions(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ChatSessionEntity> sessions = chatManager.getSessions(userId, page, size);
        return ApiResponse.success(sessions.getContent());
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<ChatSessionEntity> session(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String sessionId) {
        return chatManager.getSessionById(sessionId, userId)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("SESSION_NOT_FOUND", null, "会话不存在"));
    }

    @PutMapping("/sessions/{sessionId}")
    public ApiResponse<Void> updateSession(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String title = body.get("title");
        if (title != null) {
            chatManager.updateSessionTitle(sessionId, userId, title);
        }
        return ApiResponse.success(null);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String sessionId) {
        boolean deleted = chatManager.deleteSession(sessionId, userId);
        if (deleted) {
            return ApiResponse.success(null);
        }
        return ApiResponse.error("SESSION_NOT_FOUND", null, "会话不存在");
    }

    // ==================== 消息历史 ====================

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessageEntity>> messages(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String sessionId) {
        return ApiResponse.success(chatManager.getMessagesAsc(sessionId));
    }

    // ==================== 用户记忆 ====================

    @GetMapping("/memories")
    public ApiResponse<List<UserMemoryEntity>> memories(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(required = false) String category) {
        List<UserMemoryEntity> list = (category != null)
                ? userMemoryService.getMemories(userId, category)
                : userMemoryService.getMemories(userId);
        return ApiResponse.success(list);
    }

    @PostMapping("/memories")
    public ApiResponse<UserMemoryEntity> addMemory(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestBody AddMemoryRequest request) {
        UserMemoryEntity entity = userMemoryService.addMemory(
                userId, request.getCategory(), request.getContent(), request.getWeight());
        return ApiResponse.success(entity);
    }

    @DeleteMapping("/memories/{category}")
    public ApiResponse<Void> clearMemories(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @PathVariable String category) {
        userMemoryService.clearCategory(userId, category);
        return ApiResponse.success(null);
    }

    // ==================== 请求体 ====================

    @lombok.Data
    public static class AddMemoryRequest {
        @jakarta.validation.constraints.NotBlank
        private String category;
        @jakarta.validation.constraints.NotBlank
        private String content;
        private int weight = 50;
    }
}
