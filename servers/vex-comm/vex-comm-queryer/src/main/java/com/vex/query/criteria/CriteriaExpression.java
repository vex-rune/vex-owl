package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 表达式
 * <p>
 * 表示单个条件表达式，包含字段名、操作符和值
 * <p>
 * 示例：
 * <pre>
 *     CriteriaExpression.eq("status", "ACTIVE")
 *     CriteriaExpression.exp("username", "john")
 *     CriteriaExpression.between("age", 18, 65)
 * </pre>
 *
 * @see CriteriaPredicate
 * @see Operator
 * @see QueryCriterion
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriteriaExpression implements QueryCriterion {
    /**
     * 字段名
     */
    private String field;

    /**
     * 操作符
     */
    private Operator op;

    /**
     * 值
     * <p>
     * 支持以下类型：
     * <ul>
     *   <li>{@link String} - 字符串值</li>
     *   <li>{@link Number} - 数值（Integer, Long, Double, BigDecimal 等）</li>
     *   <li>{@link Boolean} - 布尔值</li>
     *   <li>{@link java.time.temporal.Temporal} - 日期时间（LocalDate, LocalDateTime 等）</li>
     *   <li>{@code Object[]} - 数组（用于 IN、BETWEEN 等操作符）</li>
     *   <li>{@code null} - 空值（用于 IS_NULL、IS_NOT_NULL 操作符）</li>
     * </ul>
     * <p>
     * 注意：具体类型应与字段的实际数据类型匹配，否则可能导致查询错误
     */
    private Object value;

    /**
     * 创建自定义表达式
     *
     * @param field 字段名
     * @param op    操作符
     * @param value 值
     * @return CriteriaExpression 实例
     * @throws IllegalArgumentException 如果参数不合法
     */
    public static CriteriaExpression of(String field, Operator op, Object value) {
        // 校验参数
        ExpressionValidator.validate(field, op, value);
        
        CriteriaExpression expr = new CriteriaExpression();
        expr.field = field;
        expr.op = op;
        expr.value = value;
        return expr;
    }

    /**
     * 创建等于条件表达式
     *
     * @param field 字段名
     * @param value 要匹配的值
     * @return CriteriaExpression 实例，表示 field = value
     */
    public static CriteriaExpression eq(String field, Object value) {
        return of(field, Operator.eq, value);
    }

    /**
     * 创建不等于条件表达式
     *
     * @param field 字段名
     * @param value 要排除的值
     * @return CriteriaExpression 实例，表示 field != value
     */
    public static CriteriaExpression neq(String field, Object value) {
        return of(field, Operator.neq, value);
    }

    /**
     * 创建大于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return CriteriaExpression 实例，表示 field > value
     */
    public static CriteriaExpression gt(String field, Object value) {
        return of(field, Operator.gt, value);
    }

    /**
     * 创建大于等于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return CriteriaExpression 实例，表示 field >= value
     */
    public static CriteriaExpression gte(String field, Object value) {
        return of(field, Operator.gte, value);
    }

    /**
     * 创建小于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return CriteriaExpression 实例，表示 field < value
     */
    public static CriteriaExpression lt(String field, Object value) {
        return of(field, Operator.lt, value);
    }

    /**
     * 创建小于等于条件表达式
     *
     * @param field 字段名
     * @param value 比较值
     * @return CriteriaExpression 实例，表示 field <= value
     */
    public static CriteriaExpression lte(String field, Object value) {
        return of(field, Operator.lte, value);
    }

    /**
     * 创建模糊匹配条件表达式
     *
     * @param field 字段名
     * @param value 匹配模式（支持通配符）
     * @return CriteriaExpression 实例，表示 field LIKE value
     */
    public static CriteriaExpression exp(String field, Object value) {
        return of(field, Operator.exp, value);
    }

    /**
     * 创建 IN 条件表达式
     *
     * @param field 字段名
     * @param value 值集合（数组或列表）
     * @return CriteriaExpression 实例，表示 field IN (values)
     */
    public static CriteriaExpression in(String field, Object value) {
        return of(field, Operator.in, value);
    }

    /**
     * 创建范围条件表达式
     *
     * @param field 字段名
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return CriteriaExpression 实例，表示 min <= field <= max
     */
    public static CriteriaExpression between(String field, Object min, Object max) {
        return of(field, Operator.between, new Object[]{min, max});
    }

    /**
     * 创建为空条件表达式
     *
     * @param field 字段名
     * @return CriteriaExpression 实例，表示 field IS NULL
     */
    public static CriteriaExpression isNull(String field) {
        return of(field, Operator.is_null, null);
    }

    /**
     * 创建不为空条件表达式
     *
     * @param field 字段名
     * @return CriteriaExpression 实例，表示 field IS NOT NULL
     */
    public static CriteriaExpression isNotNull(String field) {
        return of(field, Operator.is_not_null, null);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", field, op, value != null ? value : "NULL");
    }
}
