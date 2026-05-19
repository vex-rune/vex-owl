package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QueryCriteria 单元测试
 */
@DisplayName("查询条件测试")
class QueryCriteriaTest {

    @Nested
    @DisplayName("创建查询对象")
    class CreateQuery {

        @Test
        @DisplayName("创建空查询对象")
        void testOf() {
            QueryCriteria query = QueryCriteria.of();
            
            assertNotNull(query);
            assertNull(query.getSelect());
            assertNull(query.getExclude());
            assertNull(query.getFilter());
            assertNull(query.getOrderBy());
            assertNull(query.getPaging());
        }

        @Test
        @DisplayName("创建带字段选择的查询对象")
        void testQuery() {
            QueryCriteria query = QueryCriteria.query("id", "username", "email");
            
            assertNotNull(query);
            assertArrayEquals(new String[]{"id", "username", "email"}, query.getSelect());
        }
    }

    @Nested
    @DisplayName("链式调用")
    class ChainCall {

        @Test
        @DisplayName("设置选择字段")
        void testSelect() {
            QueryCriteria query = QueryCriteria.of().select("id", "name");
            
            assertArrayEquals(new String[]{"id", "name"}, query.getSelect());
        }

        @Test
        @DisplayName("设置排除字段")
        void testExclude() {
            QueryCriteria query = QueryCriteria.of().exclude("password", "salt");
            
            assertArrayEquals(new String[]{"password", "salt"}, query.getExclude());
        }

        @Test
        @DisplayName("设置过滤条件")
        void testFilter() {
            CriteriaPredicate filter = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE")
            );
            QueryCriteria query = QueryCriteria.of().filter(filter);
            
            assertEquals(filter, query.getFilter());
        }

        @Test
        @DisplayName("设置排序规则")
        void testOrderBy() {
            SortOrder sort1 = SortOrder.asc("createTime");
            SortOrder sort2 = SortOrder.desc("updateTime");
            QueryCriteria query = QueryCriteria.of().orderBy(sort1, sort2);
            
            assertEquals(2, query.getOrderBy().length);
            assertEquals(sort1, query.getOrderBy()[0]);
            assertEquals(sort2, query.getOrderBy()[1]);
        }

        @Test
        @DisplayName("设置分页信息")
        void testPaging() {
            PageRequest paging = PageRequest.of(0, 20);
            QueryCriteria query = QueryCriteria.of().paging(paging);
            
            assertEquals(paging, query.getPaging());
        }

        @Test
        @DisplayName("完整的链式调用")
        void testFullChain() {
            QueryCriteria query = QueryCriteria.of()
                .select("id", "username", "email")
                .exclude("password")
                .filter(CriteriaPredicate.and(
                    CriteriaExpression.eq("status", "ACTIVE")
                ))
                .orderBy(SortOrder.desc("createTime"))
                .paging(PageRequest.of(0, 20));

            assertNotNull(query.getSelect());
            assertNotNull(query.getExclude());
            assertNotNull(query.getFilter());
            assertNotNull(query.getOrderBy());
            assertNotNull(query.getPaging());
        }
    }

    @Nested
    @DisplayName("复杂查询场景")
    class ComplexQuery {

        @Test
        @DisplayName("构建用户搜索查询")
        void testUserSearchQuery() {
            QueryCriteria query = QueryCriteria.of()
                .select("id", "username", "email", "age")
                .exclude("password", "salt")
                .filter(CriteriaPredicate.and(
                    CriteriaExpression.exp("username", "%张%"),
                    CriteriaExpression.gte("age", 18),
                    CriteriaExpression.lte("age", 65),
                    CriteriaPredicate.or(
                        CriteriaExpression.eq("status", "ACTIVE"),
                        CriteriaExpression.eq("status", "PENDING")
                    )
                ))
                .orderBy(
                    SortOrder.desc("createTime"),
                    SortOrder.asc("username")
                )
                .paging(PageRequest.of(0, 20));

            // 验证基本字段
            assertEquals(4, query.getSelect().length);
            assertEquals(2, query.getExclude().length);
            
            // 验证过滤条件
            assertNotNull(query.getFilter());
            assertEquals(Logic.and, query.getFilter().getLogic());
            assertEquals(4, query.getFilter().getExpressions().length);
            
            // 验证排序
            assertEquals(2, query.getOrderBy().length);
            
            // 验证分页
            assertEquals(0, query.getPaging().getPage());
            assertEquals(20, query.getPaging().getSize());
        }

        @Test
        @DisplayName("构建商品筛选查询")
        void testProductFilterQuery() {
            QueryCriteria query = QueryCriteria.of()
                .select("id", "name", "price", "stock")
                .filter(CriteriaPredicate.and(
                    CriteriaExpression.between("price", 100, 1000),
                    CriteriaExpression.gt("stock", 0),
                    CriteriaExpression.in("category", new String[]{"electronics", "books"}),
                    CriteriaExpression.isNotNull("imageUrl")
                ))
                .orderBy(SortOrder.asc("price"))
                .paging(PageRequest.of(1, 50));

            assertNotNull(query.getFilter());
            assertEquals(4, query.getFilter().getExpressions().length);
            assertEquals(1, query.getPaging().getPage());
            assertEquals(50, query.getPaging().getSize());
        }
    }
}
