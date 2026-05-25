package com.vex.owl.ai.domain.chat;

import java.util.List;

import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * 对话消息管理
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiChatMessageManager {

    private final EntityManager entityManager;
    private final AiChatMessageRepository repository;

    public List<AiChatMessageEntity> query(QueriesPageRequest queriesPageRequest) {
        log.debug("对话消息通用查询, request: {}", queriesPageRequest);
        return JpaQueriesExecutor.of(AiChatMessageEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    public void saveAll(List<AiChatMessageEntity> entities) {
        repository.saveAll(entities);
    }

    public List<AiChatMessageEntity> query(String conversationId, int i) {
        return repository.findByConversationIdOrderByCreateTimeDesc(conversationId, PageRequest.of(0, i));
    }


}
