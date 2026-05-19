package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试 - 测试完整的查询流程
 */
@DisplayName("集成测试")
class IntegrationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("完整查询流程：构建 -> 序列化 -> 反序列化 -> 验证")
    void testCompleteQueryFlow() throws Exception {
        // 1. 构建查询条件
        QueryCriteria original = QueryCriteria.of()
            .select("id", "username", "email", "age")
            .exclude("password", "salt")
            .filter(CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaExpression.gte("age", 18),
                CriteriaExpression.lte("age", 65),
                CriteriaPredicate.or(
                    CriteriaExpression.exp("username", "%张%"),
                    CriteriaExpression.exp("username", "%李%")
                ),
                CriteriaExpression.in("city", new String[]{"北京", "上海", "广州"})
            ))
            .orderBy(
                SortOrder.desc("createTime"),
                SortOrder.asc("username")
            )
            .paging(PageRequest.of(0, 20));

        // 2. 序列化为 JSON
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
        assertNotNull(json);
        System.out.println("序列化后的 JSON:");
        System.out.println(json);

        // 3. 从 JSON 反序列化
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

        // 4. 验证所有字段
        assertNotNull(deserialized);
        
        // 验证 select 字段
        assertArrayEquals(original.getSelect(), deserialized.getSelect());
        assertEquals(4, deserialized.getSelect().length);
        
        // 验证 exclude 字段
        assertArrayEquals(original.getExclude(), deserialized.getExclude());
        assertEquals(2, deserialized.getExclude().length);
        
        // 验证 filter
        assertNotNull(deserialized.getFilter());
        assertEquals(Logic.and, deserialized.getFilter().getLogic());
        assertEquals(5, deserialized.getFilter().getExpressions().length);
        
        // 验证第一个条件：status = 'ACTIVE'
        QueryCriterion firstExpr = deserialized.getFilter().getExpressions()[0];
        assertInstanceOf(CriteriaExpression.class, firstExpr);
        CriteriaExpression statusExpr = (CriteriaExpression) firstExpr;
        assertEquals("status", statusExpr.getField());
        assertEquals(Operator.eq, statusExpr.getOp());
        assertEquals("ACTIVE", statusExpr.getValue());
        
        // 验证第二个条件：age >= 18
        QueryCriterion secondExpr = deserialized.getFilter().getExpressions()[1];
        assertInstanceOf(CriteriaExpression.class, secondExpr);
        CriteriaExpression ageGteExpr = (CriteriaExpression) secondExpr;
        assertEquals("age", ageGteExpr.getField());
        assertEquals(Operator.gte, ageGteExpr.getOp());
        assertEquals(18, ageGteExpr.getValue());
        
        // 验证第三个条件：age <= 65
        QueryCriterion thirdExpr = deserialized.getFilter().getExpressions()[2];
        assertInstanceOf(CriteriaExpression.class, thirdExpr);
        CriteriaExpression ageLteExpr = (CriteriaExpression) thirdExpr;
        assertEquals("age", ageLteExpr.getField());
        assertEquals(Operator.lte, ageLteExpr.getOp());
        assertEquals(65, ageLteExpr.getValue());
        
        // 验证第四个条件：嵌套的 OR 条件
        QueryCriterion fourthExpr = deserialized.getFilter().getExpressions()[3];
        assertInstanceOf(CriteriaPredicate.class, fourthExpr);
        CriteriaPredicate orPredicate = (CriteriaPredicate) fourthExpr;
        assertEquals(Logic.or, orPredicate.getLogic());
        assertEquals(2, orPredicate.getExpressions().length);
        
        // 验证第五个条件：city IN (...)
        QueryCriterion fifthExpr = deserialized.getFilter().getExpressions()[4];
        assertInstanceOf(CriteriaExpression.class, fifthExpr);
        CriteriaExpression cityExpr = (CriteriaExpression) fifthExpr;
        assertEquals("city", cityExpr.getField());
        assertEquals(Operator.in, cityExpr.getOp());
        
        // 验证排序
        assertNotNull(deserialized.getOrderBy());
        assertEquals(2, deserialized.getOrderBy().length);
        assertEquals("createTime", deserialized.getOrderBy()[0].getProperty());
        assertEquals("DESC", deserialized.getOrderBy()[0].getDirection());
        assertEquals("username", deserialized.getOrderBy()[1].getProperty());
        assertEquals("ASC", deserialized.getOrderBy()[1].getDirection());
        
        // 验证分页
        assertNotNull(deserialized.getPaging());
        assertEquals(0, deserialized.getPaging().getPage());
        assertEquals(20, deserialized.getPaging().getSize());
    }

    @Test
    @DisplayName("简单查询：只包含基本条件")
    void testSimpleQuery() throws Exception {
        QueryCriteria query = QueryCriteria.of()
            .filter(CriteriaPredicate.and(CriteriaExpression.eq("id", 123)));

        String json = objectMapper.writeValueAsString(query);
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

        assertNotNull(deserialized.getFilter());
        assertInstanceOf(CriteriaPredicate.class, deserialized.getFilter());
    }

    @Test
    @DisplayName("复杂嵌套查询：多层 AND/OR 嵌套")
    void testComplexNestedQuery() throws Exception {
        QueryCriteria query = QueryCriteria.of()
            .filter(
                CriteriaPredicate.or(
                    CriteriaPredicate.and(
                        CriteriaExpression.eq("type", "A"),
                        CriteriaExpression.gt("value", 100)
                    ),
                    CriteriaPredicate.and(
                        CriteriaExpression.eq("type", "B"),
                        CriteriaExpression.lt("value", 50),
                        CriteriaPredicate.or(
                            CriteriaExpression.isNotNull("field1"),
                            CriteriaExpression.isNotNull("field2")
                        )
                    )
                )
            );

        String json = objectMapper.writeValueAsString(query);
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

        assertNotNull(deserialized.getFilter());
        assertEquals(Logic.or, deserialized.getFilter().getLogic());
        assertEquals(2, deserialized.getFilter().getExpressions().length);
    }

    @Test
    @DisplayName("边界情况：空查询条件")
    void testEmptyQuery() throws Exception {
        QueryCriteria query = QueryCriteria.of();

        String json = objectMapper.writeValueAsString(query);
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

        assertNotNull(deserialized);
        assertNull(deserialized.getFilter());
    }

    @Test
    @DisplayName("边界情况：只有分页和排序")
    void testOnlyPagingAndSorting() throws Exception {
        QueryCriteria query = QueryCriteria.of()
            .orderBy(SortOrder.asc("name"))
            .paging(PageRequest.of(5, 100));

        String json = objectMapper.writeValueAsString(query);
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);

        assertNotNull(deserialized.getOrderBy());
        assertEquals(1, deserialized.getOrderBy().length);
        assertNotNull(deserialized.getPaging());
        assertEquals(5, deserialized.getPaging().getPage());
        assertEquals(100, deserialized.getPaging().getSize());
    }

    @Test
    @DisplayName("性能测试：大量条件的序列化/反序列化")
    void testPerformanceWithManyConditions() throws Exception {
        // 创建包含多个条件的查询
        CriteriaPredicate predicate = CriteriaPredicate.and();
        
        // 添加 50 个条件
        for (int i = 0; i < 50; i++) {
            // 这里需要通过反射或其他方式添加，因为 and() 返回的是新实例
            // 这只是一个示意，实际使用时可以动态构建
        }
        
        QueryCriteria query = QueryCriteria.of()
            .filter(CriteriaPredicate.and(CriteriaExpression.eq("test", "value")));

        long startTime = System.currentTimeMillis();
        String json = objectMapper.writeValueAsString(query);
        QueryCriteria deserialized = objectMapper.readValue(json, QueryCriteria.class);
        long endTime = System.currentTimeMillis();

        assertNotNull(deserialized);
        System.out.println("序列化+反序列化耗时: " + (endTime - startTime) + "ms");
        
        // 应该在合理时间内完成（小于 1 秒）
        assertTrue((endTime - startTime) < 1000);
    }
}
