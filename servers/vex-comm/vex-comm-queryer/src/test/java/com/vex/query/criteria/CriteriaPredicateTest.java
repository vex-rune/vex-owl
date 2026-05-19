package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CriteriaPredicate 单元测试
 */
@DisplayName("条件组合测试")
class CriteriaPredicateTest {

    @Nested
    @DisplayName("创建条件组合")
    class CreatePredicate {

        @Test
        @DisplayName("创建 AND 组合")
        void testAnd() {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaExpression.gte("age", 18)
            );

            assertEquals(Logic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
        }

        @Test
        @DisplayName("创建 OR 组合")
        void testOr() {
            CriteriaPredicate predicate = CriteriaPredicate.or(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaExpression.eq("status", "PENDING")
            );

            assertEquals(Logic.or, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
        }

        @Test
        @DisplayName("创建 NOT 组合")
        void testNot() {
            CriteriaPredicate original = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "DELETED")
            );
            CriteriaPredicate notPredicate = original.not();

            assertEquals(Logic.not, notPredicate.getLogic());
            assertEquals(1, notPredicate.getExpressions().length);
        }

        @Test
        @DisplayName("使用 of 方法创建默认 AND 组合")
        void testOf() {
            CriteriaPredicate predicate = CriteriaPredicate.of(
                CriteriaExpression.eq("status", "ACTIVE")
            );

            assertEquals(Logic.and, predicate.getLogic());
            assertEquals(1, predicate.getExpressions().length);
        }
    }

    @Nested
    @DisplayName("嵌套条件组合")
    class NestedPredicate {

        @Test
        @DisplayName("创建嵌套的 AND-OR 组合")
        void testNestedAndOr() {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaPredicate.or(
                    CriteriaExpression.exp("username", "%张%"),
                    CriteriaExpression.exp("username", "%李%")
                )
            );

            assertEquals(Logic.and, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            // 验证嵌套的 OR 条件
            QueryCriterion secondExpr = predicate.getExpressions()[1];
            assertInstanceOf(CriteriaPredicate.class, secondExpr);
            CriteriaPredicate nestedPredicate = (CriteriaPredicate) secondExpr;
            assertEquals(Logic.or, nestedPredicate.getLogic());
        }

        @Test
        @DisplayName("创建多层嵌套组合")
        void testMultiLevelNested() {
            CriteriaPredicate predicate = CriteriaPredicate.or(
                CriteriaPredicate.and(
                    CriteriaExpression.eq("type", "A"),
                    CriteriaExpression.gt("value", 10)
                ),
                CriteriaPredicate.and(
                    CriteriaExpression.eq("type", "B"),
                    CriteriaExpression.lt("value", 20)
                )
            );

            assertEquals(Logic.or, predicate.getLogic());
            assertEquals(2, predicate.getExpressions().length);
            
            // 验证两个嵌套的 AND 条件
            for (QueryCriterion expr : predicate.getExpressions()) {
                assertInstanceOf(CriteriaPredicate.class, expr);
                CriteriaPredicate nested = (CriteriaPredicate) expr;
                assertEquals(Logic.and, nested.getLogic());
                assertEquals(2, nested.getExpressions().length);
            }
        }
    }

    @Nested
    @DisplayName("空值检查")
    class EmptyCheck {

        @Test
        @DisplayName("空表达式数组判断为空")
        void testEmptyWithNullArray() {
            CriteriaPredicate predicate = new CriteriaPredicate();
            assertTrue(predicate.checkEmpty());
        }

        @Test
        @DisplayName("空数组判断为空")
        void testEmptyWithEmptyArray() {
            CriteriaPredicate predicate = CriteriaPredicate.and();
            assertTrue(predicate.checkEmpty());
        }

        @Test
        @DisplayName("有表达式判断不为空")
        void testNotEmpty() {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE")
            );
            assertFalse(predicate.checkEmpty());
        }
    }

    @Nested
    @DisplayName("实现 QueryCriterion 接口")
    class ImplementInterface {

        @Test
        @DisplayName("非空谓词 checkEmpty 返回 false")
        void testCheckEmptyWhenNotEmpty() {
            CriteriaPredicate predicate = CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE")
            );
            assertFalse(predicate.checkEmpty());
        }
    }
}
