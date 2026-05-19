package com.vex.query.criteria.jpa;

import com.vex.query.criteria.QueriesCondition;
import com.vex.query.criteria.QueriesCriteria;
import com.vex.query.criteria.QueriesOperatorEnum;
import com.vex.query.criteria.QueriesPredicate;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 查询构建器
 * <p>
 * 将 {@link QueriesPredicate} 转换为 JPA 的 {@link CriteriaQuery}，
 * 支持动态查询条件、字段选择、排序等功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><b>字段选择</b>：支持 select（包含）和 exclude（排除）字段</li>
 *   <li><b>过滤条件</b>：支持多种操作符（eq, gt, lt, in 等）</li>
 *   <li><b>排序</b>：支持多字段升序/降序排序</li>
 *   <li><b>类型安全</b>：在构建时进行严格的类型校验</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 1. 创建查询条件
 * QueryPredicate predicate = new QueryPredicate();
 * predicate.setAnd(List.of(
 *     Criteria.builder()
 *         .condition(new Condition("age", OperatorEnum.gte, 18))
 *         .build()
 * ));
 *
 * // 2. 构建 JPA 查询
 * CriteriaQuery<User> query = CriteriaQueryBuilder.buildQuery(
 *     User.class, predicate, entityManager.getCriteriaBuilder()
 * );
 *
 * // 3. 执行查询
 * List<User> users = entityManager.createQuery(query).getResultList();
 * }</pre>
 *
 * @param <T> 实体类型
 * @author vex
 * @since 1.0.0
 */
public class CriteriaQueryBuilder<T> {

    private final Class<T> entityClass;
    private Root<T> root;
    private CriteriaQuery<T> query;
    private CriteriaBuilder cb;

    /**
     * 私有构造函数，通过静态工厂方法创建实例
     *
     * @param entityClass 实体类类型
     */
    private CriteriaQueryBuilder(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 为指定实体类创建查询构建器
     *
     * @param entityClass 实体类类型
     * @param <T>        实体类型
     * @return 查询构建器实例
     */
    public static <T> CriteriaQueryBuilder<T> forEntity(Class<T> entityClass) {
        return new CriteriaQueryBuilder<>(entityClass);
    }

    /**
     * 初始化 CriteriaQuery
     *
     * @param cb JPA CriteriaBuilder
     * @return 当前构建器实例（支持链式调用）
     */
    public CriteriaQueryBuilder<T> from(CriteriaBuilder cb) {
        this.cb = cb;
        this.query = cb.createQuery(entityClass);
        return this;
    }

    /**
     * 获取查询根对象
     *
     * @return Root 对象
     */
    public Root<T> getRoot() {
        return root;
    }

    /**
     * 获取构建好的 CriteriaQuery
     *
     * @return CriteriaQuery 对象
     */
    public CriteriaQuery<T> getQuery() {
        return query;
    }

    /**
     * 应用查询条件到 CriteriaQuery
     * <p>
     * 依次应用过滤条件和排序规则。
     * </p>
     *
     * @param predicate 查询条件对象
     * @return 当前构建器实例（支持链式调用）
     */
    public CriteriaQueryBuilder<T> apply(QueriesPredicate predicate) {
        this.root = query.from(entityClass);
        applyFilter(predicate);
        return this;
    }

    /**
     * 应用过滤条件
     * <p>
     * 将 Predicate 转换为 JPA Predicate，并设置到查询的 where 子句中。
     * 如果过滤条件为空，则不添加 where 子句。
     * </p>
     *
     * @param predicate 过滤条件
     */
    private void applyFilter(QueriesPredicate predicate) {
        if (predicate == null || (predicate.getAnd() == null || predicate.getAnd().isEmpty())
            && (predicate.getOr() == null || predicate.getOr().isEmpty())) {
            query.where();
            return;
        }

        List<Predicate> predicates = new ArrayList<>();

        if (predicate.getAnd() != null && !predicate.getAnd().isEmpty()) {
            predicates.add(buildPredicateFromCriteria(predicate.getAnd(), true));
        }

        if (predicate.getOr() != null && !predicate.getOr().isEmpty()) {
            predicates.add(buildPredicateFromCriteria(predicate.getOr(), false));
        }

        if (predicates.isEmpty()) {
            query.where();
        } else if (predicates.size() == 1) {
            query.where(predicates.get(0));
        } else {
            query.where(cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
        }
    }

    /**
     * 从 Criteria 列表构建 JPA Predicate
     *
     * @param criteriaList 条件列表
     * @param isAnd      是否为 AND 逻辑
     * @return JPA Predicate
     */
    private Predicate buildPredicateFromCriteria(List<QueriesCriteria> criteriaList, boolean isAnd) {
        List<Predicate> predicates = new ArrayList<>();

        for (QueriesCriteria criteria : criteriaList) {
            if (criteria.getCondition() != null) {
                predicates.add(buildPredicateFromCondition(criteria.getCondition()));
            } else if (criteria.getPredicate() != null) {
                predicates.add(buildPredicateFromCriteria(criteria.getPredicate().getAnd(), true));
            }
        }

        if (predicates.isEmpty()) {
            return cb.conjunction();
        }

        return isAnd
            ? cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]))
            : cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    }

    /**
     * 从 Condition 构建 JPA Predicate
     *
     * @param condition 条件
     * @return JPA Predicate
     */
    private Predicate buildPredicateFromCondition(QueriesCondition condition) {
        String fieldName = condition.getField();
        QueriesOperatorEnum op = condition.getOp();
        Object value = condition.getValue();

        final Path<Object> objectPath = getPath(fieldName);

        // 使用策略模式：验证值类型并构建 Predicate
        QueryStrategy strategy = QueryStrategyFactory.getStrategy(op);
        return strategy.build(cb, objectPath, value, op);
    }

    /**
     * 获取字段的 Path 对象
     *
     * @param fieldName 字段名
     * @return 字段的 Path 对象
     * @throws IllegalArgumentException 当字段不存在时抛出
     */
    private Path<Object> getPath(String fieldName) {
        try {
            return root.get(fieldName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Field '" + fieldName + "' does not exist in entity '" +
                entityClass.getSimpleName() + "'", e);
        }
    }


    /**
     * 静态工厂方法：构建完整的 CriteriaQuery
     *
     * @param entityClass 实体类类型
     * @param predicate  查询条件对象
     * @param cb        JPA CriteriaBuilder
     * @param <T>       实体类型
     * @return 构建好的 CriteriaQuery
     */
    public static <T> CriteriaQuery<T> buildQuery(
            Class<T> entityClass,
            QueriesPredicate predicate,
            CriteriaBuilder cb) {
        return forEntity(entityClass)
                .from(cb)
                .apply(predicate)
                .getQuery();
    }
}
