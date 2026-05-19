package com.vex.query.criteria;

/**
 * <p>
 * 定义比较操作符的通用行为，所有操作符枚举需实现此接口
 *
 * @see Operator
 */
public enum Operator {

    /**
     * 等于
     */
    eq,

    /**
     * 不等于
     */
    neq,

    /**
     * 大于
     */
    gt,

    /**
     * 大于等于
     */
    gte,

    /**
     * 小于
     */
    lt,

    /**
     * 小于等于
     */
    lte,

    /**
     * 模糊匹配
     */
    exp,

    /**
     * 模糊匹配（取反）
     */
    not_exp,

    /**
     * 在集合中
     */
    in,

    /**
     * 不在集合中
     */
    not_in,

    /**
     * 范围
     */
    between,

    /**
     * 为空
     */
    is_null,

    /**
     * 不为空
     */
    is_not_null;

}
