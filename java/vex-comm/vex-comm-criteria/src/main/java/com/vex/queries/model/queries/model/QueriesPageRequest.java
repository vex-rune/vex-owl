package com.vex.queries.model.queries.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询条件
 * <p>
 * 表示单个查询条件，包含字段名、操作符和值
 *
 * @author vex
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueriesPageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 查询条件
     */
    private QueriesPredicate predicate;

    /**
     * 排序条件
     */
    private QueriesOrder order;

    /**
     * 分页条件
     */
    private QueriesPage page;
}