package com.vex.owl.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.vex.queries.model.queries.model.QueriesCondition;
import com.vex.queries.model.queries.model.QueriesCriteria;
import com.vex.queries.model.queries.model.QueriesOperatorEnum;
import com.vex.queries.model.queries.model.QueriesPage;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import com.vex.queries.model.queries.model.QueriesPredicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AiChatMessageManager.class)
@Tag("integration")
@DisplayName("AiChatMessageManager 数据库集成测试")
class AiChatMessageManagerTest {

    @Autowired
    private AiChatMessageManager aiChatMessageManager;

    private static final String TENANT_A = "tenant-a";
    private static final String TENANT_B = "tenant-b";
    private static final String CONV_1 = "conv-1";
    private static final String CONV_2 = "conv-2";

    @BeforeEach
    void setUp() {
        AiChatMessageEntity msg1 = AiChatMessageEntity.builder()
                .tenantId(TENANT_A)
                .conversationId(CONV_1)
                .messageType("USER")
                .textContent("你好")
                .build();

        AiChatMessageEntity msg2 = AiChatMessageEntity.builder()
                .tenantId(TENANT_A)
                .conversationId(CONV_1)
                .messageType("ASSISTANT")
                .textContent("你好，有什么可以帮助你的？")
                .build();

        AiChatMessageEntity msg3 = AiChatMessageEntity.builder()
                .tenantId(TENANT_A)
                .conversationId(CONV_1)
                .messageType("USER")
                .textContent("帮我查一下订单")
                .build();

        AiChatMessageEntity msg4 = AiChatMessageEntity.builder()
                .tenantId(TENANT_A)
                .conversationId(CONV_2)
                .messageType("SYSTEM")
                .textContent("系统初始化")
                .build();

        AiChatMessageEntity msg5 = AiChatMessageEntity.builder()
                .tenantId(TENANT_B)
                .conversationId(CONV_2)
                .messageType("USER")
                .textContent("另一个租户的消息")
                .build();

        aiChatMessageManager.saveAll(List.of(msg1, msg2, msg3, msg4, msg5));
    }

    @Test
    @DisplayName("查询全部 — 无过滤条件，返回所有消息")
    void shouldReturnAllMessages() {
        QueriesPageRequest request = new QueriesPageRequest();
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("按 conversationId 等值查询 — 返回指定会话的消息")
    void shouldFilterByConversationId() {
        QueriesCondition condition = new QueriesCondition();
        condition.setField("conversationId");
        condition.setOp(QueriesOperatorEnum.eq);
        condition.setValue(CONV_1);

        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setCondition(condition);

        QueriesPredicate predicate = new QueriesPredicate();
        predicate.setAnd(List.of(criteria));

        QueriesPageRequest request = new QueriesPageRequest();
        request.setPredicate(predicate);
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(m -> CONV_1.equals(m.getConversationId()));
    }

    @Test
    @DisplayName("按 messageType 等值查询 — 返回指定类型的消息")
    void shouldFilterByMessageType() {
        QueriesCondition condition = new QueriesCondition();
        condition.setField("messageType");
        condition.setOp(QueriesOperatorEnum.eq);
        condition.setValue("USER");

        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setCondition(condition);

        QueriesPredicate predicate = new QueriesPredicate();
        predicate.setAnd(List.of(criteria));

        QueriesPageRequest request = new QueriesPageRequest();
        request.setPredicate(predicate);
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(m -> "USER".equals(m.getMessageType()));
    }

    @Test
    @DisplayName("多条件 AND 组合查询 — 租户 + 会话")
    void shouldFilterByMultipleConditions() {
        QueriesCondition condition1 = new QueriesCondition();
        condition1.setField("tenantId");
        condition1.setOp(QueriesOperatorEnum.eq);
        condition1.setValue(TENANT_A);

        QueriesCondition condition2 = new QueriesCondition();
        condition2.setField("conversationId");
        condition2.setOp(QueriesOperatorEnum.eq);
        condition2.setValue(CONV_2);

        QueriesCriteria criteria1 = new QueriesCriteria();
        criteria1.setCondition(condition1);
        QueriesCriteria criteria2 = new QueriesCriteria();
        criteria2.setCondition(condition2);

        QueriesPredicate predicate = new QueriesPredicate();
        predicate.setAnd(List.of(criteria1, criteria2));

        QueriesPageRequest request = new QueriesPageRequest();
        request.setPredicate(predicate);
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTextContent()).isEqualTo("系统初始化");
    }

    @Test
    @DisplayName("like 模糊查询 — 按 textContent 模糊匹配")
    void shouldLikeFilterByTextContent() {
        QueriesCondition condition = new QueriesCondition();
        condition.setField("textContent");
        condition.setOp(QueriesOperatorEnum.like);
        condition.setValue("%订单%");

        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setCondition(condition);

        QueriesPredicate predicate = new QueriesPredicate();
        predicate.setAnd(List.of(criteria));

        QueriesPageRequest request = new QueriesPageRequest();
        request.setPredicate(predicate);
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTextContent()).contains("订单");
    }

    @Test
    @DisplayName("分页查询 — 只返回指定页的数据")
    void shouldPaginate() {
        QueriesPageRequest request = new QueriesPageRequest();
        request.setPage(new QueriesPage(0, 2));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("空条件查询 — predicate 为 null 时返回全部")
    void shouldReturnAllWhenPredicateIsNull() {
        QueriesPageRequest request = new QueriesPageRequest();
        request.setPage(new QueriesPage(0, 20));

        List<AiChatMessageEntity> result = aiChatMessageManager.query(request);

        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("按会话 ID 查询最近消息 — 返回指定数量")
    void shouldQueryRecentMessagesByConversationId() {
        List<AiChatMessageEntity> result = aiChatMessageManager.query(CONV_1, 2);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(m -> CONV_1.equals(m.getConversationId()));
    }

    @Test
    @DisplayName("按会话 ID 查询 — 数量大于实际消息数时返回全部")
    void shouldReturnAllMessagesWhenLimitExceedsCount() {
        List<AiChatMessageEntity> result = aiChatMessageManager.query(CONV_2, 100);

        assertThat(result).hasSize(2);
    }
}
