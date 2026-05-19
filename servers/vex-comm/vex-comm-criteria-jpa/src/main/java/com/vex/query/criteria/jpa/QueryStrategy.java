package com.vex.query.criteria.jpa;

import com.vex.query.criteria.QueriesOperatorEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

/**
 * 查询条件策略接口
 * <p>
 * 每个操作符对应一个策略，负责验证值类型并构建 JPA Predicate
 *
 * @author vex
 * @since 1.0.0
 */
@FunctionalInterface
public interface QueryStrategy {

    /**
     * 验证值类型并构建 Predicate
     *
     * @param cb       JPA CriteriaBuilder
     * @param path     字段路径
     * @param value    查询值
     * @param operator 操作符（用于错误提示）
     * @return JPA Predicate
     * @throws IllegalArgumentException 当值类型不匹配时抛出
     */
    Predicate build(CriteriaBuilder cb, Path<?> path, Object value, QueriesOperatorEnum operator);
}
