package com.vex.owl.ai.domain.chat;

import com.vex.queries.model.queries.model.QueriesPageRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Cache;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatMessageMemory implements ChatMemory {
    private final AiChatMessageManager aiChatMessageManager;

    @Override
    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public void add(String conversationId, List<Message> messages) {
        aiChatMessageManager.saveAll(messages.stream().map(this::toEntity).toList());
    }


    @Override
    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public List<Message> get(String conversationId) {
        // 只获取 50 条
        return aiChatMessageManager.query(conversationId, 50)
                .stream()
                .map(this::toMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public void clear(String conversationId) {
    }

    private Optional<Message> toMessage(AiChatMessageEntity entity) {
        String content = entity.getTextContent() != null ? entity.getTextContent() : "";
        return switch (entity.getMessageType()) {
            case "USER" -> Optional.of(new UserMessage(content));
            case "ASSISTANT" -> Optional.of(new AssistantMessage(content));
            case "SYSTEM" -> Optional.of(new SystemMessage(content));
            default -> Optional.empty();
        };
    }

    private AiChatMessageEntity toEntity(Message message) {
        AiChatMessageEntity entity = new AiChatMessageEntity();
        entity.setTextContent(message.getText());
        entity.setMessageType(message.getClass().getSimpleName());
//        entity.setMetadata(message.getMetadata());
        return entity;
    }
}
