package com.vex.owl.ai.app.agent;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.UserMemoryEntity;
import com.vex.owl.ai.domain.chat.UserMemoryService;
import com.vex.owl.ai.domain.event.ChatMessageCreateEvent;
import com.vex.owl.ai.domain.tools.AgentAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * 记忆 Advisor
 *
 * <p>在 LLM 调用前加载上下文记忆，调用后发送消息保存事件：</p>
 * <ul>
 *   <li>对话记忆：加载当前 session 最近 N 条消息作为上下文</li>
 *   <li>用户记忆：加载租户级别的长期记忆（偏好、事实等）</li>
 * </ul>
 *
 * <p>通过 ChatClientRequest.context() 获取 userId、sessionId、memoryIndex 等参数。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryAdvisor implements AgentAdvisor {

    public static final String NAME = "MemoryAdvisor";
    /** 在 TokenUsageAdvisor 之后执行 */
    public static final int ORDER = 3;

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION_ID = "sessionId";
    private static final String KEY_MEMORY_INDEX = "memoryIndex";
    private static final String KEY_HISTORY_SIZE = "historySize";

    private static final int DEFAULT_HISTORY_SIZE = 20;
    private static final int DEFAULT_MEMORY_INDEX = 10;

    private final ChatManager chatManager;
    private final UserMemoryService userMemoryService;
    private final EventPublisher eventPublisher;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        Map<String, Object> ctx = request.context();
        String userId = getString(ctx, KEY_USER_ID);
        String sessionId = getString(ctx, KEY_SESSION_ID);

        // 向 prompt instructions 添加记忆上下文
        injectMemoryContext(request, userId, sessionId, ctx);

        log.debug("MemoryAdvisor | userId={} | sessionId={}", userId, sessionId);

        // 执行调用
        ChatClientResponse response = chain.nextCall(request);

        // 发送消息保存事件
        publishMessageEvent(userId, sessionId, ctx);

        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        Map<String, Object> ctx = request.context();
        String userId = getString(ctx, KEY_USER_ID);
        String sessionId = getString(ctx, KEY_SESSION_ID);

        // 向 prompt instructions 添加记忆上下文
        injectMemoryContext(request, userId, sessionId, ctx);

        log.debug("MemoryAdvisor(stream) | userId={} | sessionId={}", userId, sessionId);

        return chain.nextStream(request);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    // ==================== 注入记忆上下文 ====================

    private void injectMemoryContext(ChatClientRequest request, String userId, String sessionId, Map<String, Object> ctx) {
        // 1. 添加用户长期记忆
        String userMemory = buildUserMemorySection(userId, ctx);
        if (userMemory != null && !userMemory.isEmpty()) {
            request.prompt().getInstructions().add(new AssistantMessage(userMemory));
        }

        // 2. 添加对话历史
        String history = buildHistorySection(sessionId, ctx);
        if (history != null && !history.isEmpty()) {
            request.prompt().getInstructions().add(new AssistantMessage(history));
        }
    }

    private String buildUserMemorySection(String userId, Map<String, Object> ctx) {
        if (userId == null || userId.isEmpty()) return null;

        int memoryIndex = getInt(ctx, KEY_MEMORY_INDEX, DEFAULT_MEMORY_INDEX);

        List<UserMemoryEntity> memories = userMemoryService.getMemories(userId);
        if (memories.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("【用户记忆】");

        int count = 0;
        for (UserMemoryEntity mem : memories) {
            if (memoryIndex > 0 && count >= memoryIndex) break;
            sb.append("\n- [").append(mem.getCategory()).append("] ").append(mem.getContent());
            count++;
        }

        return sb.toString();
    }

    private String buildHistorySection(String sessionId, Map<String, Object> ctx) {
        if (sessionId == null || sessionId.isEmpty()) return null;

        int historySize = getInt(ctx, KEY_HISTORY_SIZE, DEFAULT_HISTORY_SIZE);
        List<com.vex.owl.ai.domain.chat.ChatMessageEntity> historyEntities = chatManager.getMessages(sessionId, historySize);

        if (historyEntities.isEmpty()) return null;

        List<Message> history = historyEntities.stream()
                .sorted(Comparator.comparing(com.vex.owl.ai.domain.chat.ChatMessageEntity::getCreateTime))
                .map(this::toMessage)
                .filter(Objects::nonNull)
                .toList();

        if (history.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】");

        for (Message msg : history) {
            String role = msg instanceof UserMessage ? "用户" : "助手";
            sb.append("\n").append(role).append(": ").append(msg.getText());
        }

        return sb.toString();
    }

    // ==================== 发送消息事件 ====================

    private void publishMessageEvent(String userId, String sessionId, Map<String, Object> ctx) {
        String userText = extractPromptText(ctx);
        if (sessionId == null || sessionId.isEmpty() || userText == null || userText.isEmpty()) return;

        eventPublisher.publish("ChatMessageCreateEvent", ChatMessageCreateEvent.builder()
                .userId(userId)
                .sessionId(sessionId)
                .messageType("USER")
                .textContent(userText)
                .build());
    }

    // ==================== 工具方法 ====================

    private String extractPromptText(Map<String, Object> ctx) {
        Object input = ctx.get("input");
        if (input != null) return input.toString();
        Object prompt = ctx.get("prompt");
        if (prompt != null) return prompt.toString();
        return "";
    }

    private Message toMessage(com.vex.owl.ai.domain.chat.ChatMessageEntity entity) {
        String content = entity.getTextContent() != null ? entity.getTextContent() : "";
        if (content.isEmpty()) return null;
        return switch (entity.getMessageType()) {
            case "USER" -> new UserMessage(content);
            case "ASSISTANT" -> new AssistantMessage(content);
            default -> null;
        };
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private static int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number n) return n.intValue();
        return defaultValue;
    }
}
