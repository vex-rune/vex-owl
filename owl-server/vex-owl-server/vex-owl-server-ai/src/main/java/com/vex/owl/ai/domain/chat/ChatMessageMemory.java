package com.vex.owl.ai.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatMessageMemory implements ChatMemory {
    private final ChatManager chatManager;

    @Override
//    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public void add(String conversationId, List<Message> messages) {
        chatManager.saveMessages(messages.stream().map(m -> toEntity(conversationId, m)).toList());
    }


    @Override
//    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public List<Message> get(String conversationId) {
        // 只获取 50 条
        return chatManager.getMessages(conversationId, 50)
                .stream()
                .map(this::toMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
//    @CacheEvict(value = "chatMemory", key = "#conversationId")
    public void clear(String conversationId) {
    }

    private Optional<Message> toMessage(ChatMessageEntity entity) {
        String content = entity.getTextContent() != null ? entity.getTextContent() : "";
        return switch (entity.getMessageType()) {
            case "USER" -> Optional.of(new UserMessage(content));
            case "ASSISTANT" -> Optional.of(new AssistantMessage(content));
            case "SYSTEM" -> Optional.of(new SystemMessage(content));
            default -> Optional.empty();
        };
    }

    private ChatMessageEntity toEntity(String conversationId, Message message) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setConversationId(conversationId);
        entity.setTextContent(message.getText());
        entity.setMessageType(extractMessageType(message.getClass().getSimpleName()));
        entity.setMessageId(message.getMetadata().getOrDefault("id", "").toString());
        return entity;
    }

    private String extractMessageType(String className) {
        return switch (className) {
            case "UserMessage" -> "USER";
            case "AssistantMessage" -> "ASSISTANT";
            case "SystemMessage" -> "SYSTEM";
            default -> className;
        };
    }
}
