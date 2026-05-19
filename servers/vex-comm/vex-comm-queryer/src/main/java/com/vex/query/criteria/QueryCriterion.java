package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 查询条件节点接口
 * <p>
 * 所有查询条件相关的类都需要实现此接口，包括：
 * <ul>
 *   <li>{@link CriteriaExpression} - 单个条件表达式</li>
 *   <li>{@link CriteriaPredicate} - 条件组合（谓词）</li>
 * </ul>
 * <p>
 * 这样可以在 {@link CriteriaPredicate} 中使用统一类型，提高类型安全性
 * <p>
 * 注意：为了支持 JSON 序列化/反序列化，使用了 Jackson 的类型注解。
 * 序列化时会自动添加 "type" 字段标识具体类型，
 * 反序列化时根据该字段创建正确的实例。
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CriteriaExpression.class, name = "expression"),
    @JsonSubTypes.Type(value = CriteriaPredicate.class, name = "predicate")
})
public interface QueryCriterion {
    
    /**
     * 判断条件是否为空
     *
     * @return true 表示条件为空，false 表示条件有效
     */
    default boolean checkEmpty() {
        return false;
    }
}
