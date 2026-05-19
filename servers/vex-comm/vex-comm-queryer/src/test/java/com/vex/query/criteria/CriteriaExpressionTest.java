package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CriteriaExpression 单元测试
 */
@DisplayName("条件表达式测试")
class CriteriaExpressionTest {

    @Nested
    @DisplayName("创建表达式")
    class CreateExpression {

        @Test
        @DisplayName("创建等于表达式")
        void testEq() {
            CriteriaExpression expr = CriteriaExpression.eq("status", "ACTIVE");
            
            assertEquals("status", expr.getField());
            assertEquals(Operator.eq, expr.getOp());
            assertEquals("ACTIVE", expr.getValue());
        }

        @Test
        @DisplayName("创建不等于表达式")
        void testNeq() {
            CriteriaExpression expr = CriteriaExpression.neq("status", "DELETED");
            
            assertEquals("status", expr.getField());
            assertEquals(Operator.neq, expr.getOp());
            assertEquals("DELETED", expr.getValue());
        }

        @Test
        @DisplayName("创建大于表达式")
        void testGt() {
            CriteriaExpression expr = CriteriaExpression.gt("age", 18);
            
            assertEquals("age", expr.getField());
            assertEquals(Operator.gt, expr.getOp());
            assertEquals(18, expr.getValue());
        }

        @Test
        @DisplayName("创建大于等于表达式")
        void testGte() {
            CriteriaExpression expr = CriteriaExpression.gte("score", 60);
            
            assertEquals("score", expr.getField());
            assertEquals(Operator.gte, expr.getOp());
            assertEquals(60, expr.getValue());
        }

        @Test
        @DisplayName("创建小于表达式")
        void testLt() {
            CriteriaExpression expr = CriteriaExpression.lt("age", 65);
            
            assertEquals("age", expr.getField());
            assertEquals(Operator.lt, expr.getOp());
            assertEquals(65, expr.getValue());
        }

        @Test
        @DisplayName("创建小于等于表达式")
        void testLte() {
            CriteriaExpression expr = CriteriaExpression.lte("price", 100);
            
            assertEquals("price", expr.getField());
            assertEquals(Operator.lte, expr.getOp());
            assertEquals(100, expr.getValue());
        }

        @Test
        @DisplayName("创建模糊匹配表达式")
        void testExp() {
            CriteriaExpression expr = CriteriaExpression.exp("username", "%张%");
            
            assertEquals("username", expr.getField());
            assertEquals(Operator.exp, expr.getOp());
            assertEquals("%张%", expr.getValue());
        }

        @Test
        @DisplayName("创建 IN 表达式")
        void testIn() {
            String[] values = {"ACTIVE", "PENDING"};
            CriteriaExpression expr = CriteriaExpression.in("status", values);
            
            assertEquals("status", expr.getField());
            assertEquals(Operator.in, expr.getOp());
            assertArrayEquals(values, (String[]) expr.getValue());
        }

        @Test
        @DisplayName("创建 BETWEEN 表达式")
        void testBetween() {
            CriteriaExpression expr = CriteriaExpression.between("age", 18, 65);
            
            assertEquals("age", expr.getField());
            assertEquals(Operator.between, expr.getOp());
            Object[] range = (Object[]) expr.getValue();
            assertEquals(18, range[0]);
            assertEquals(65, range[1]);
        }

        @Test
        @DisplayName("创建 IS NULL 表达式")
        void testIsNull() {
            CriteriaExpression expr = CriteriaExpression.isNull("deletedAt");
            
            assertEquals("deletedAt", expr.getField());
            assertEquals(Operator.is_null, expr.getOp());
            assertNull(expr.getValue());
        }

        @Test
        @DisplayName("创建 IS NOT NULL 表达式")
        void testIsNotNull() {
            CriteriaExpression expr = CriteriaExpression.isNotNull("email");
            
            assertEquals("email", expr.getField());
            assertEquals(Operator.is_not_null, expr.getOp());
            assertNull(expr.getValue());
        }

        @Test
        @DisplayName("使用 of 方法创建自定义表达式")
        void testOf() {
            CriteriaExpression expr = CriteriaExpression.of("field", Operator.eq, "value");
            
            assertEquals("field", expr.getField());
            assertEquals(Operator.eq, expr.getOp());
            assertEquals("value", expr.getValue());
        }
    }

    @Nested
    @DisplayName("实现 QueryCriterion 接口")
    class ImplementInterface {

        @Test
        @DisplayName("表达式不为空")
        void testIsEmpty() {
            CriteriaExpression expr = CriteriaExpression.eq("status", "ACTIVE");
            assertFalse(expr.checkEmpty());
        }
    }
}
