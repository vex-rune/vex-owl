package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 表达式值校验器测试
 */
@DisplayName("ExpressionValidator 测试")
class ExpressionValidatorTest {

    @Nested
    @DisplayName("比较操作符校验（EQ, NEQ, GT, GTE, LT, LTE）")
    class ComparisonOperatorValidation {

        @Test
        @DisplayName("字符串值 - 合法")
        void testStringValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.eq("name", "张三");
            });
        }

        @Test
        @DisplayName("整数值 - 合法")
        void testIntegerValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.gte("age", 18);
            });
        }

        @Test
        @DisplayName("双精度值 - 合法")
        void testDoubleValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.lt("price", 99.99);
            });
        }

        @Test
        @DisplayName("布尔值 - 合法")
        void testBooleanValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.eq("enabled", true);
            });
        }

        @Test
        @DisplayName("日期时间值 - 合法")
        void testTemporalValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.gte("createTime", LocalDateTime.now());
            });
        }

        @Test
        @DisplayName("NULL 值 - 非法")
        void testNullValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.eq("name", null)
            );
            assertTrue(exception.getMessage().contains("cannot have null value"));
        }

        @Test
        @DisplayName("不支持的类型 - 非法")
        void testUnsupportedTypeInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("data", Operator.eq, new Object())
            );
            assertTrue(exception.getMessage().contains("requires String, Number, Boolean, or Temporal"));
        }
    }

    @Nested
    @DisplayName("模糊匹配操作符校验（EXP, NOT_EXP）")
    class PatternMatchingValidation {

        @Test
        @DisplayName("字符串值 - 合法")
        void testStringValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.exp("username", "%张%");
            });
        }

        @Test
        @DisplayName("非字符串值 - 非法")
        void testNonStringValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.exp("username", 123)
            );
            assertTrue(exception.getMessage().contains("requires String value"));
        }

        @Test
        @DisplayName("NULL 值 - 非法")
        void testNullValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.exp("username", null)
            );
            assertTrue(exception.getMessage().contains("cannot have null value"));
        }
    }

    @Nested
    @DisplayName("IN/NOT_IN 操作符校验")
    class InOperatorValidation {

        @Test
        @DisplayName("数组值 - 合法")
        void testArrayValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.in("status", new String[]{"ACTIVE", "PENDING"});
            });
        }

        @Test
        @DisplayName("Collection 值 - 合法")
        void testCollectionValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.in("status", Arrays.asList("ACTIVE", "PENDING"));
            });
        }

        @Test
        @DisplayName("非数组值 - 非法")
        void testNonArrayValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.in("status", "ACTIVE")
            );
            assertTrue(exception.getMessage().contains("requires Array or Collection"));
        }

        @Test
        @DisplayName("NULL 值 - 非法")
        void testNullValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.in("status", null)
            );
            assertTrue(exception.getMessage().contains("cannot have null value"));
        }
    }

    @Nested
    @DisplayName("BETWEEN 操作符校验")
    class BetweenOperatorValidation {

        @Test
        @DisplayName("两个值的数组 - 合法")
        void testTwoElementArrayValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.between("age", 18, 65);
            });
        }

        @Test
        @DisplayName("非数组值 - 非法")
        void testNonArrayValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("age", Operator.between, "18-65")
            );
            assertTrue(exception.getMessage().contains("requires Array value"));
        }

        @Test
        @DisplayName("数组长度不为2 - 非法")
        void testWrongArrayLengthInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("age", Operator.between, new Object[]{18})
            );
            assertTrue(exception.getMessage().contains("requires exactly 2 values"));
        }

        @Test
        @DisplayName("数组中包含 NULL - 非法")
        void testNullInArrayInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("age", Operator.between, new Object[]{null, 65})
            );
            assertTrue(exception.getMessage().contains("cannot have null values"));
        }
    }

    @Nested
    @DisplayName("IS_NULL/IS_NOT_NULL 操作符校验")
    class NullCheckValidation {

        @Test
        @DisplayName("NULL 值 - 合法")
        void testNullValueValid() {
            assertDoesNotThrow(() -> {
                CriteriaExpression.isNull("deletedAt");
            });
        }

        @Test
        @DisplayName("非 NULL 值 - 非法")
        void testNonNullValueInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("deletedAt", Operator.is_null, "not null")
            );
            assertTrue(exception.getMessage().contains("must have null value"));
        }
    }

    @Nested
    @DisplayName("字段名和操作符校验")
    class FieldAndOperatorValidation {

        @Test
        @DisplayName("空字段名 - 非法")
        void testEmptyFieldNameInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.eq("", "value")
            );
            assertTrue(exception.getMessage().contains("Field name cannot be null or empty"));
        }

        @Test
        @DisplayName("NULL 字段名 - 非法")
        void testNullFieldNameInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.eq(null, "value")
            );
            assertTrue(exception.getMessage().contains("Field name cannot be null or empty"));
        }

        @Test
        @DisplayName("NULL 操作符 - 非法")
        void testNullOperatorInvalid() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CriteriaExpression.of("field", null, "value")
            );
            assertTrue(exception.getMessage().contains("Operator cannot be null"));
        }
    }
}
