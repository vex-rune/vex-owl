package com.vex.query.criteria;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询条件项
 * <p>
 * 条件项是查询条件的最小单位，支持两种形式：
 * <ul>
 *   <li>{@link QueriesCondition} - 简单条件</li>
 *   <li>{@link QueriesPredicate} - 嵌套子查询</li>
 * </ul>
 * 二选一，不能同时存在
 *
 * @author vex
 * @since 1.0.0
 */
@Data
public class QueriesCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** 条件 */
    private QueriesCondition condition;

    /** 子查询 */
    private QueriesPredicate predicate;
}