package com.vex.query.criteria.jpa;

import com.vex.query.criteria.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询执行器
 * <p>执行 JPA 查询操作，支持分页、列表和计数查询</p>
 * <p>整合了查询构建、策略管理和执行逻辑</p>
 *
 * @param <E> 实体类型（必须继承 IdentifiableEntity）
 * @author vex
 * @since 3.0.0
 */
public class JpaQueriesExecutor<E extends JpaBasicWithIdEntity> {

    private final Class<E> entityClass;
    private final EntityManager entityManager;
    private QueriesPredicate predicate;
    private List<QueriesOrder> orders;

    /**
     * 策略缓存表 - 线程安全
     */
    private static final Map<QueriesOperatorEnum, Function<PredicateContext, Predicate>> STRATEGY_CACHE = new ConcurrentHashMap<>();

    static {
        initializeStrategies();
    }

    /**
     * 谓词构建上下文
     */
    private record PredicateContext(CriteriaBuilder builder, Path<?> path, Object value, QueriesOperatorEnum operator) {
    }

    /**
     * 初始化所有策略
     */
    private static void initializeStrategies() {
        Arrays.stream(QueriesOperatorEnum.values())
                .forEach(operator -> STRATEGY_CACHE.put(operator, createStrategy(operator)));
    }

    /**
     * 创建具体策略 - 根据操作符类型创建 Lambda 表达式
     */
    private static Function<PredicateContext, Predicate> createStrategy(QueriesOperatorEnum operator) {
        return switch (operator) {
            // 等于 / 不等于
            case eq -> ctx -> ctx.builder().equal(ctx.path(), ctx.value());
            case ne -> ctx -> ctx.builder().notEqual(ctx.path(), ctx.value());

            // 大于 / 大于等于（需 Comparable 类型）
            case gt -> ctx -> {
                validateComparable(ctx.value(), ctx.operator());
                return ctx.builder().greaterThan(ctx.path().as((Class<Comparable>) ctx.value().getClass()), (Comparable) ctx.value());
            };
            case gte -> ctx -> {
                validateComparable(ctx.value(), ctx.operator());
                return ctx.builder().greaterThanOrEqualTo(ctx.path().as((Class<Comparable>) ctx.value().getClass()), (Comparable) ctx.value());
            };

            // 小于 / 小于等于（需 Comparable 类型）
            case lt -> ctx -> {
                validateComparable(ctx.value(), ctx.operator());
                return ctx.builder().lessThan(ctx.path().as((Class<Comparable>) ctx.value().getClass()), (Comparable) ctx.value());
            };
            case lte -> ctx -> {
                validateComparable(ctx.value(), ctx.operator());
                return ctx.builder().lessThanOrEqualTo(ctx.path().as((Class<Comparable>) ctx.value().getClass()), (Comparable) ctx.value());
            };

            // 模糊匹配（LIKE，需 String 类型）
            case like -> ctx -> {
                validateString(ctx.value(), ctx.operator());
                return ctx.builder().like(ctx.path().as(String.class), (String) ctx.value());
            };

            // 正则表达式匹配（需 String 类型）
            case exp -> ctx -> {
                validateString(ctx.value(), ctx.operator());
                return ctx.builder().equal(ctx.builder().function("regexp", Boolean.class, ctx.path(), ctx.builder().literal(ctx.value())), true);
            };

            // 为空 / 非空判断（值必须为 null）
            case isNull -> ctx -> {
                validateNull(ctx.value(), ctx.operator());
                return ctx.builder().isNull(ctx.path());
            };
            case isNotNull -> ctx -> {
                validateNull(ctx.value(), ctx.operator());
                return ctx.builder().isNotNull(ctx.path());
            };

            // IN 查询（需 Iterable 或数组类型）
            case in -> ctx -> {
                validateCollection(ctx.value(), ctx.operator());
                return buildInPredicate(ctx.path(), ctx.value());
            };
        };
    }

    /**
     * 验证方法组 - 类型安全检查
     */
    private static void validateComparable(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " 操作符需要非空的 Comparable 值");
        }
        if (!(value instanceof Comparable)) {
            throw new IllegalArgumentException(operator + " 操作符需要 Comparable 类型，实际类型: " + value.getClass().getName());
        }
    }

    private static void validateString(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " 操作符需要非空的 String 值");
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(operator + " 操作符需要 String 类型，实际类型: " + value.getClass().getName());
        }
    }

    private static void validateNull(Object value, QueriesOperatorEnum operator) {
        if (value != null) {
            throw new IllegalArgumentException(operator + " 操作符不需要传入值");
        }
    }

    private static void validateCollection(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " 操作符需要非空的集合或数组");
        }
        if (!(value instanceof Iterable) && !value.getClass().isArray()) {
            throw new IllegalArgumentException(operator + " 操作符需要 Iterable 或数组类型，实际类型: " + value.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static Predicate buildInPredicate(Path<?> path, Object value) {
        if (value instanceof Iterable) {
            return path.in((Iterable<?>) value);
        }
        if (value instanceof Object[]) {
            return path.in(Arrays.asList((Object[]) value));
        }
        if (value instanceof int[]) {
            return path.in(Arrays.stream((int[]) value).boxed().toList());
        }
        if (value instanceof long[]) {
            return path.in(Arrays.stream((long[]) value).boxed().toList());
        }
        if (value instanceof double[]) {
            return path.in(Arrays.stream((double[]) value).boxed().toList());
        }
        throw new IllegalArgumentException("IN 操作符需要 Iterable 或数组类型");
    }

    /**
     * 构造函数
     *
     * @param entityClass    实体类类型
     * @param entityManager  JPA EntityManager
     */
    public JpaQueriesExecutor(Class<E> entityClass, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.entityManager = entityManager;
    }

    /**
     * 创建查询执行器实例（静态工厂方法）
     *
     * @param entityClass    实体类类型
     * @param entityManager  JPA EntityManager
     * @param <T>            实体类型
     * @return 查询执行器实例
     */
    public static <T extends JpaBasicWithIdEntity> JpaQueriesExecutor<T> of(Class<T> entityClass, EntityManager entityManager) {
        return new JpaQueriesExecutor<>(entityClass, entityManager);
    }

    /**
     * 使用完整分页请求执行分页查询
     *
     * @param pageRequest 分页请求参数（包含 predicate、order、page）
     * @return 分页结果列表
     */
    public List<E> page(QueriesPageRequest pageRequest) {
        if (pageRequest != null) {
            if (pageRequest.getPredicate() != null) {
                this.predicate = pageRequest.getPredicate();
            }
            if (pageRequest.getOrder() != null) {
                this.orders = Collections.singletonList(pageRequest.getOrder());
            }
            if (pageRequest.getPage() != null) {
                return page(pageRequest.getPage());
            }
        }
        return list();
    }

    /**
     * 分页查询
     * <p>根据分页参数执行分页查询，支持排序</p>
     *
     * @param page 分页参数
     * @return 分页结果列表
     */
    public List<E> page(QueriesPage page) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        
        // 构建查询
        CriteriaQuery<E> query = buildSelectQuery(builder);
        
        // 应用排序
        applyOrdering(query, builder);
        
        // 执行分页查询
        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(page.getPage() * page.getSize());
        typedQuery.setMaxResults(page.getSize());
        
        return typedQuery.getResultList();
    }

    /**
     * 列表查询
     * <p>执行列表查询，默认返回前 10 条记录</p>
     *
     * @return 实体列表
     */
    public List<E> list() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = buildSelectQuery(builder);
        
        // 应用排序
        applyOrdering(query, builder);
        
        // 限制返回数量
        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(0);
        typedQuery.setMaxResults(10);
        
        return typedQuery.getResultList();
    }

    /**
     * 计数查询
     * <p>统计符合条件的记录总数</p>
     *
     * @return 记录总数
     */
    public long count() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        // 构建计数查询
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<E> root = countQuery.from(entityClass);

        // 应用查询条件
        applyWhereClause(countQuery, root, builder);

        // 选择计数字段
        countQuery.select(builder.count(root.get(JpaBasicWithIdEntity.ID_FIELD)));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * 带条件的计数查询
     * <p>根据指定的查询条件统计记录总数</p>
     *
     * @param predicate 查询条件
     * @return 记录总数
     */
    public long count(QueriesPredicate predicate) {
        this.predicate = predicate;
        return count();
    }

    /**
     * 检查是否存在符合指定条件的记录
     *
     * @param predicate 查询条件
     * @return true 如果存在至少一条记录
     */
    public boolean exists(QueriesPredicate predicate) {
        return count(predicate) > 0;
    }

    /**
     * 构建 SELECT 查询
     */
    private CriteriaQuery<E> buildSelectQuery(CriteriaBuilder builder) {
        CriteriaQuery<E> query = builder.createQuery(entityClass);
        Root<E> root = query.from(entityClass);
        
        // 应用 WHERE 条件
        applyWhereClause(query, root, builder);
        
        return query;
    }

    /**
     * 应用 WHERE 子句
     */
    private void applyWhereClause(CriteriaQuery<?> query, Root<?> root, CriteriaBuilder builder) {
        if (predicate == null || isEmptyPredicate(predicate)) {
            query.where();
            return;
        }

        List<Predicate> predicates = new ArrayList<>();

        // 处理 AND 条件
        if (predicate.getAnd() != null && !predicate.getAnd().isEmpty()) {
            predicates.add(buildPredicateFromCriteria(predicate.getAnd(), true, root, builder));
        }

        // 处理 OR 条件
        if (predicate.getOr() != null && !predicate.getOr().isEmpty()) {
            predicates.add(buildPredicateFromCriteria(predicate.getOr(), false, root, builder));
        }

        // 组合所有 Predicate
        if (predicates.isEmpty()) {
            query.where();
        } else if (predicates.size() == 1) {
            query.where(predicates.get(0));
        } else {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }
    }

    /**
     * 从 Criteria 列表构建 Predicate
     */
    private Predicate buildPredicateFromCriteria(List<QueriesCriteria> criteriaList, boolean isAnd, Root<?> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        for (QueriesCriteria criteria : criteriaList) {
            if (criteria.getCondition() != null) {
                predicates.add(buildPredicateFromCondition(criteria.getCondition(), root, builder));
            } else if (criteria.getPredicate() != null) {
                predicates.add(buildPredicateFromCriteria(
                        criteria.getPredicate().getAnd(), true, root, builder));
            }
        }

        if (predicates.isEmpty()) {
            return builder.conjunction();
        }

        return isAnd
                ? builder.and(predicates.toArray(new Predicate[0]))
                : builder.or(predicates.toArray(new Predicate[0]));
    }

    /**
     * 从 Condition 构建 Predicate
     */
    private Predicate buildPredicateFromCondition(QueriesCondition condition, Root<?> root, CriteriaBuilder builder) {
        String fieldName = condition.getField();
        QueriesOperatorEnum operator = condition.getOp();
        Object value = condition.getValue();

        Path<Object> path = root.get(fieldName);

        // 使用策略模式构建 Predicate
        Function<PredicateContext, Predicate> strategy = STRATEGY_CACHE.get(operator);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的操作符: " + operator);
        }
        return strategy.apply(new PredicateContext(builder, path, value, operator));
    }

    /**
     * 应用排序条件
     */
    private void applyOrdering(CriteriaQuery<E> query, CriteriaBuilder builder) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Root<E> root = query.from(entityClass);
        List<Order> orderList = orders.stream()
                .map(order -> {
                    switch (order.getOrder()) {
                        case DESC:
                            return builder.desc(root.get(order.getFieldName()));
                        case ASC:
                        default:
                            return builder.asc(root.get(order.getFieldName()));
                    }
                })
                .collect(Collectors.toList());

        query.orderBy(orderList);
    }

    /**
     * 检查 Predicate 是否为空
     */
    private boolean isEmptyPredicate(QueriesPredicate predicate) {
        return predicate == null ||
                ((predicate.getAnd() == null || predicate.getAnd().isEmpty()) &&
                 (predicate.getOr() == null || predicate.getOr().isEmpty()));
    }
}
