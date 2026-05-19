package com.vex.query.criteria;

/**
 * 表达式值校验器
 * <p>
 * 用于验证 CriteriaExpression 中 value 的类型是否符合 operator 的要求
 */
public class ExpressionValidator {

    /**
     * 校验表达式的值类型是否合法
     *
     * @param field 字段名
     * @param op    操作符
     * @param value 值
     * @throws IllegalArgumentException 如果值类型不合法
     */
    public static void validate(String field, Operator op, Object value) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Field name cannot be null or empty");
        }

        if (op == null) {
            throw new IllegalArgumentException("Operator cannot be null");
        }

        switch (op) {
            case eq:
            case neq:
            case gt:
            case gte:
            case lt:
            case lte:
                validateComparisonValue(field, op, value);
                break;
            case exp:
            case not_exp:
                validateStringValue(field, op, value);
                break;
            case in:
            case not_in:
                validateArrayValue(field, op, value);
                break;
            case between:
                validateBetweenValue(field, value);
                break;
            case is_null:
            case is_not_null:
                validateNullValue(field, op, value);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + op);
        }
    }

    /**
     * 校验比较操作的值（EQ, NEQ, GT, GTE, LT, LTE）
     */
    private static void validateComparisonValue(String field, Operator op, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' cannot have null value", field, op)
            );
        }

        // 支持字符串、数字、布尔值、日期时间
        if (!(value instanceof String || 
              value instanceof Number || 
              value instanceof Boolean ||
              value instanceof java.time.temporal.Temporal)) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' requires String, Number, Boolean, or Temporal value, but got: %s",
                    field, op, value.getClass().getName())
            );
        }
    }

    /**
     * 校验模糊匹配的值（EXP, NOT_EXP）
     */
    private static void validateStringValue(String field, Operator op, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' cannot have null value", field, op)
            );
        }

        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' requires String value, but got: %s",
                    field, op, value.getClass().getName())
            );
        }
    }

    /**
     * 校验数组值（IN, NOT_IN）
     */
    private static void validateArrayValue(String field, Operator op, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' cannot have null value", field, op)
            );
        }

        if (!value.getClass().isArray() && !(value instanceof java.util.Collection)) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' requires Array or Collection value, but got: %s",
                    field, op, value.getClass().getName())
            );
        }
    }

    /**
     * 校验 BETWEEN 的值
     */
    private static void validateBetweenValue(String field, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator 'between' cannot have null value", field)
            );
        }

        if (!value.getClass().isArray()) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator 'between' requires Array value [min, max], but got: %s",
                    field, value.getClass().getName())
            );
        }

        Object[] range = (Object[]) value;
        if (range.length != 2) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator 'between' requires exactly 2 values [min, max], but got: %d",
                    field, range.length)
            );
        }

        if (range[0] == null || range[1] == null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator 'between' cannot have null values in range", field)
            );
        }
    }

    /**
     * 校验 NULL 检查的值（IS_NULL, IS_NOT_NULL）
     */
    private static void validateNullValue(String field, Operator op, Object value) {
        if (value != null) {
            throw new IllegalArgumentException(
                String.format("Field '%s' with operator '%s' must have null value", field, op)
            );
        }
    }
}
