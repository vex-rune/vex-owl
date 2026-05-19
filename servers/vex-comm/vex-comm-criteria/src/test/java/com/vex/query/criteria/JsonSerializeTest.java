package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JSON序列化测试")
class JsonSerializeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Condition序列化")
    void conditionSerialize() throws Exception {
        QueriesCondition condition = new QueriesCondition();
        condition.setField("name");
        condition.setOp(QueriesOperatorEnum.eq);
        condition.setValue("test");

        String json = objectMapper.writeValueAsString(condition);
        assertNotNull(json);
        assertTrue(json.contains("\"field\":\"name\""));
        assertTrue(json.contains("\"op\":\"eq\""));
        assertTrue(json.contains("\"value\":\"test\""));
    }

    @Test
    @DisplayName("Condition反序列化")
    void conditionDeserialize() throws Exception {
        String json = "{\"field\":\"age\",\"op\":\"gt\",\"value\":18}";

        QueriesCondition condition = objectMapper.readValue(json, QueriesCondition.class);

        assertEquals("age", condition.getField());
        assertEquals(QueriesOperatorEnum.gt, condition.getOp());
        assertEquals(18, condition.getValue());
    }

    @Test
    @DisplayName("Criteria序列化-简单条件")
    void criteriaSerializeSimple() throws Exception {
        QueryCriteria criteria = new QueryCriteria();
        criteria.setCondition(new QueriesCondition());

        criteria.getCondition().setField("id");
        criteria.getCondition().setOp(QueriesOperatorEnum.ne);
        criteria.getCondition().setValue(0);

        String json = objectMapper.writeValueAsString(criteria);
        assertNotNull(json);
        assertTrue(json.contains("\"condition\""));
    }

    @Test
    @DisplayName("Criteria序列化-子查询")
    void criteriaSerializeSubQuery() throws Exception {
        QueryCriteria criteria = new QueryCriteria();
        criteria.setPredicate(new Predicate());

        criteria.getPredicate().setAnd(List.of());

        String json = objectMapper.writeValueAsString(criteria);
        assertNotNull(json);
        assertTrue(json.contains("\"predicate\""));
    }

    @Test
    @DisplayName("Predicate序列化")
    void predicateSerialize() throws Exception {
        Predicate predicate = new Predicate();

        QueriesCondition c1 = new QueriesCondition();
        c1.setField("status");
        c1.setOp(QueriesOperatorEnum.eq);
        c1.setValue("active");

        QueriesCondition c2 = new QueriesCondition();
        c2.setField("type");
        c2.setOp(QueriesOperatorEnum.in);

        predicate.setAnd(List.of(
                new QueryCriteria(),
                new QueryCriteria()
        ));
        predicate.getAnd().get(0).setCondition(c1);
        predicate.getAnd().get(1).setCondition(c2);

        String json = objectMapper.writeValueAsString(predicate);
        assertNotNull(json);
        assertTrue(json.contains("\"and\""));
    }

    @Test
    @DisplayName("Predicate反序列化")
    void predicateDeserialize() throws Exception {
        String json = """
                {
                    "and": [
                        {"condition": {"field": "name", "op": "like", "value": "张%"}},
                        {"predicate": {"or": []}}
                    ]
                }
                """;

        Predicate predicate = objectMapper.readValue(json, Predicate.class);

        assertNotNull(predicate.getAnd());
        assertEquals(2, predicate.getAnd().size());
    }

    @Test
    @DisplayName("OperatorEnum序列化")
    void operatorEnumSerialize() throws Exception {
        QueriesCondition condition = new QueriesCondition();
        condition.setField("id");
        condition.setOp(QueriesOperatorEnum.isNull);

        String json = objectMapper.writeValueAsString(condition);
        assertTrue(json.contains("\"op\":\"isNull\""));
    }

    @Test
    @DisplayName("OperatorEnum反序列化")
    void operatorEnumDeserialize() throws Exception {
        String json = "{\"field\":\"deleted\",\"op\":\"isNotNull\"}";

        QueriesCondition condition = objectMapper.readValue(json, QueriesCondition.class);

        assertEquals(QueriesOperatorEnum.isNotNull, condition.getOp());
    }

    @Test
    @DisplayName("完整查询条件序列化")
    void fullQuerySerialize() throws Exception {
        Predicate predicate = new Predicate();

        QueriesCondition c1 = new QueriesCondition();
        c1.setField("name");
        c1.setOp(QueriesOperatorEnum.like);
        c1.setValue("%测试%");

        QueriesCondition c2 = new QueriesCondition();
        c2.setField("age");
        c2.setOp(QueriesOperatorEnum.gte);
        c2.setValue(18);

        QueryCriteria criteria1 = new QueryCriteria();
        criteria1.setCondition(c1);

        QueryCriteria criteria2 = new QueryCriteria();
        criteria2.setCondition(c2);

        predicate.setAnd(List.of(criteria1, criteria2));

        String json = objectMapper.writeValueAsString(predicate);
        assertNotNull(json);

        // 反序列化验证
        Predicate deserialized = objectMapper.readValue(json, Predicate.class);
        assertNotNull(deserialized.getAnd());
        assertEquals(2, deserialized.getAnd().size());
    }
}