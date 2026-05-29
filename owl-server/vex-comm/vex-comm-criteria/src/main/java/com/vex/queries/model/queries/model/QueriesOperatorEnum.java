package com.vex.queries.model.queries.model;

/**
 * 查询条件操作符枚举
 * <p>
 * 定义支持的比较操作符，用于构建动态查询条件
 *
 * @author vex
 * @since 1.0.0
 */
public enum QueriesOperatorEnum {
    /** 等于 */
    eq,
    /** 不等于 */
    ne,
    /** 大于 */
    gt,
    /** 大于等于 */
    gte,
    /** 小于 */
    lt,
    /** 小于等于 */
    lte,
    /** 模糊匹配（Like） */
    like,
    /** 正则表达式（Regexp） */
    exp,
    /** 包含（In） */
    in,
    /** 为空（Is Null） */
    isNull,
    /** 不为空（Is Not Null） */
    isNotNull;
}