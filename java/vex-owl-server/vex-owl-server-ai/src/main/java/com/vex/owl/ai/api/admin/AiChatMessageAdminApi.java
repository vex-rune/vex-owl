package com.vex.owl.ai.api.admin;

import java.util.List;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.chat.AiChatMessageEntity;
import com.vex.owl.ai.domain.chat.AiChatMessageManager;
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
@RequestMapping("/api/ai/admin/chat-message")
@RequiredArgsConstructor
@Slf4j
public class AiChatMessageAdminApi {

    private final AiChatMessageManager aiChatMessageManager;

    /**
     * 对话消息-通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 对话消息列表
     */
    @PostMapping("/query")
    public ApiResponse<List<AiChatMessageEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        log.info("对话消息通用查询, request: {}", request);
        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);
        return ApiResponse.success(result);
    }
}
