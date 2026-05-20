package com.vex.query.criteria;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 查询条件组合
 * <p>
 * 用于组合多个查询条件，支持两种逻辑关系：
 * <ul>
 *   <li>{@link #and} - AND组合，所有条件都满足</li>
 *   <li>{@link #or}  - OR组合，任一条件满足</li>
 * </ul>
 *
 * @author vex
 * @since 1.0.0
 */
@Data
public class QueriesPredicate implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * AND组合
     */
    private List<QueriesCriteria> and;

    /**
     * OR组合
     */
    private List<QueriesCriteria> or;
}