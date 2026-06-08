package com.vex.owl.ai.domain.tools;

import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatMessageEntity;
import com.vex.owl.ai.domain.chat.UserMemoryEntity;
import com.vex.owl.ai.domain.chat.UserMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 记忆 Advisor
 *
 * <p>统一管理对话记忆和用户记忆：</p>
 * <ul>
 *   <li>对话记忆：加载当前 session 最近 N 条消息作为上下文</li>
 *   <li>用户记忆：加载租户级别的长期记忆（偏好、事实等）</li>
 *   <li>自动保存：调用完成后持久化 user/assistant 消息</li>
 * </ul>
 *
 * <p>通过 advisor params 获取 tenantId 和 sessionId（与 RunContext.toMap() 一致）。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryAdvisor implements AgentAdvisor {

    public static final String NAME = "MemoryAdvisor";
    public static final int ORDER = 0;
    private static final int DEFAULT_HISTORY_SIZE = 20;

    private final ChatManager chatManager;
    private final UserMemoryService userMemoryService;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Map<String, Object> ctx = request.context();
        String tenantId = getString(ctx, "tenantId");
        String sessionId = getString(ctx, "sessionId");

        // 1. 提取原始 prompt 文本
        String originalText = extractPromptText(request.prompt());

        // 2. 加载记忆并构建增强 prompt
        String enrichedText = buildEnrichedPrompt(tenantId, sessionId, originalText);
        Prompt enrichedPrompt = new Prompt(enrichedText, request.prompt().getOptions());
        ChatClientRequest enrichedRequest = request.mutate().prompt(enrichedPrompt).build();

        // 3. 执行调用
        ChatClientResponse response = chain.nextCall(enrichedRequest);

        // 4. 保存消息
        saveMessages(tenantId, sessionId, originalText, response);

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        Map<String, Object> ctx = request.context();
        String tenantId = getString(ctx, "tenantId");
        String sessionId = getString(ctx, "sessionId");

        String originalText = extractPromptText(request.prompt());
        String enrichedText = buildEnrichedPrompt(tenantId, sessionId, originalText);
        Prompt enrichedPrompt = new Prompt(enrichedText, request.prompt().getOptions());
        ChatClientRequest enrichedRequest = request.mutate().prompt(enrichedPrompt).build();

        return chain.nextStream(enrichedRequest)
                .doOnComplete(() -> {
                    if (originalText != null && !originalText.isEmpty()) {
                        saveEntity(tenantId, sessionId, originalText, "USER");
                    }
                });
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    // ==================== 构建增强 Prompt ====================

    private String buildEnrichedPrompt(String tenantId, String sessionId, String prompt) {
        StringBuilder sb = new StringBuilder();

        // 用户长期记忆
        String userMemory = buildUserMemorySection(tenantId);
        if (userMemory != null) {
            sb.append(userMemory).append("\n\n");
        }

        // 对话历史
        String history = buildHistorySection(sessionId);
        if (history != null) {
            sb.append(history).append("\n\n");
        }

        // 当前用户输入
        sb.append(prompt);

        return sb.toString();
    }

    private String buildUserMemorySection(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) return null;

        List<UserMemoryEntity> memories = userMemoryService.getMemories(tenantId);
        if (memories.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("【用户记忆】");

        for (UserMemoryEntity mem : memories) {
            sb.append("\n- [").append(mem.getCategory()).append("] ").append(mem.getContent());
        }

        return sb.toString();
    }

    private String buildHistorySection(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;

        List<Message> history = loadHistory(sessionId);
        if (history.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】");

        for (Message msg : history) {
            String role = msg instanceof UserMessage ? "用户" : "助手";
            sb.append("\n").append(role).append(": ").append(msg.getText());
        }

        return sb.toString();
    }

    private List<Message> loadHistory(String sessionId) {
        return chatManager.getMessages(sessionId, DEFAULT_HISTORY_SIZE).stream()
                .sorted(Comparator.comparing(ChatMessageEntity::getCreateTime))
                .map(this::toMessage)
                .filter(Objects::nonNull)
                .toList();
    }

    // ==================== 保存消息 ====================

    private void saveMessages(String tenantId, String sessionId,
                              String userText, ChatClientResponse response) {
        if (sessionId == null || sessionId.isEmpty()) return;

        if (userText != null && !userText.isEmpty()) {
            saveEntity(tenantId, sessionId, userText, "USER");
        }

        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse != null && chatResponse.getResult() != null) {
            String assistantText = chatResponse.getResult().getOutput().getText();
            if (assistantText != null && !assistantText.isEmpty()) {
                saveEntity(tenantId, sessionId, assistantText, "ASSISTANT");
            }
        }
    }

    private void saveEntity(String tenantId, String sessionId, String text, String type) {
        try {
            ChatMessageEntity entity = ChatMessageEntity.builder()
                    .tenantId(tenantId)
                    .conversationId(sessionId)
                    .messageType(type)
                    .textContent(text)
                    .build();
            chatManager.saveMessages(List.of(entity));
        } catch (Exception e) {
            log.warn("保存消息失败: session={}, type={}", sessionId, type, e);
        }
    }

    // ==================== 工具方法 ====================

    private String extractPromptText(Prompt prompt) {
        if (prompt == null) return "";
        List<Message> messages = prompt.getInstructions();
        if (messages == null || messages.isEmpty()) return "";
        return messages.stream()
                .filter(m -> m instanceof UserMessage)
                .map(Message::getText)
                .findFirst()
                .orElse("");
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Message toMessage(ChatMessageEntity entity) {
        String content = entity.getTextContent() != null ? entity.getTextContent() : "";
        return switch (entity.getMessageType()) {
            case "USER" -> new UserMessage(content);
            case "ASSISTANT" -> new AssistantMessage(content);
            default -> null;
        };
    }
}
