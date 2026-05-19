package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 谓词（条件组合）
 * <p>
 * 用于组合多个条件表达式，支持 AND、OR、NOT 三种逻辑运算
 * <p>
 * 示例：
 * <pre>
 *     CriteriaPredicate.and(
 *         CriteriaExpression.eq("status", "ACTIVE"),
 *         CriteriaExpression.gte("age", 18)
 *     )
 * </pre>
 *
 * @see CriteriaExpression
 * @see Logic
 * @see QueryCriterion
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CriteriaPredicate implements QueryCriterion {
    /**
     * 逻辑运算符，默认 AND
     */
    private Logic logic = Logic.and;

    /**
     * 条件节点数组
     * <p>
     * 每个元素可以是：
     * <ul>
     *   <li>{@link CriteriaExpression} - 简单条件</li>
     *   <li>{@link CriteriaPredicate} - 嵌套条件组合</li>
     * </ul>
     */
    private QueryCriterion[] expressions;

    /**
     * 判断条件组合是否为空
     *
     * @return true 表示没有条件表达式，false 表示有条件表达式
     */
    @Override
    public boolean checkEmpty() {
        return expressions == null || expressions.length == 0;
    }

    /**
     * 创建默认条件组合（AND 逻辑）
     *
     * @param expressions 条件节点数组，可以是 CriteriaExpression 或 CriteriaPredicate
     * @return CriteriaPredicate 实例，使用 AND 逻辑组合所有条件
     */
    public static CriteriaPredicate of(QueryCriterion... expressions) {
        CriteriaPredicate p = new CriteriaPredicate();
        p.logic = Logic.and;
        p.expressions = expressions;
        return p;
    }

    /**
     * 创建 AND 逻辑条件组合
     *
     * @param expressions 条件节点数组，所有条件必须同时满足
     * @return CriteriaPredicate 实例，使用 AND 逻辑组合
     */
    public static CriteriaPredicate and(QueryCriterion... expressions) {
        CriteriaPredicate p = new CriteriaPredicate();
        p.logic = Logic.and;
        p.expressions = expressions;
        return p;
    }

    /**
     * 创建 OR 逻辑条件组合
     *
     * @param expressions 条件节点数组，满足任一条件即可
     * @return CriteriaPredicate 实例，使用 OR 逻辑组合
     */
    public static CriteriaPredicate or(QueryCriterion... expressions) {
        CriteriaPredicate p = new CriteriaPredicate();
        p.logic = Logic.or;
        p.expressions = expressions;
        return p;
    }

    /**
     * 创建 NOT 逻辑条件组合（对当前条件取反）
     *
     * @return 新的 CriteriaPredicate 实例，表示对当前条件的否定
     */
    public CriteriaPredicate not() {
        CriteriaPredicate p = new CriteriaPredicate();
        p.logic = Logic.not;
        p.expressions = new QueryCriterion[]{this};
        return p;
    }

    @Override
    public String toString() {
        if (expressions == null || expressions.length == 0) {
            return logic + "()";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < expressions.length; i++) {
            if (i > 0) {
                sb.append(" ").append(logic).append(" ");
            }
            sb.append(expressions[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
