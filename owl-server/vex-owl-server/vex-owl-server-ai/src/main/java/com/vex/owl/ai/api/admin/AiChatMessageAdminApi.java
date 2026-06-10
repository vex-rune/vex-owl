package com.vex.owl.ai.api.admin;

import java.util.List;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.chat.ChatMessageEntity;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatSessionEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话消息管理
 */
@RestController
@RequestMapping("/api/ai/admin/admin")
@RequiredArgsConstructor
@Slf4j
public class AiChatMessageAdminApi {

    private final ChatManager chatManager;

    /**
     * 对话会话-通用查询
     */
    @PostMapping("/chat-session/query")
    public ApiResponse<List<ChatSessionEntity>> querySession(@Valid @RequestBody QueriesPageRequest request) {
        List<ChatSessionEntity> result = chatManager.querySession(request);
        return ApiResponse.success(result);
    }

    /**
     * 对话消息-通用查询
     */
    @PostMapping("/chat-message/query")
    public ApiResponse<List<ChatMessageEntity>> queryMessages(@Valid @RequestBody QueriesPageRequest request) {
        List<ChatMessageEntity> result = chatManager.queryMessages(request);
        return ApiResponse.success(result);
    }


}
