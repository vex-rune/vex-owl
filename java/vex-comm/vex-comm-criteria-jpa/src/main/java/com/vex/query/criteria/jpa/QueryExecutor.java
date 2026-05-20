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
public class QueryExecutor<E extends BasicWithIdJpaEntity> {

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
    public QueryExecutor(Class<E> entityClass, EntityManager entityManager) {
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
    public static <T extends BasicWithIdJpaEntity> QueryExecutor<T> of(Class<T> entityClass, EntityManager entityManager) {
        return new QueryExecutor<>(entityClass, entityManager);
    }

    /**
     * 使用分页请求参数执行查询（一站式方法）
     *
     * @param pageRequest 分页请求参数（包含 predicate、order、page）
     * @return 分页结果列表
     */
    public List<E> executeQuery(QueriesPageRequest pageRequest) {
        if (pageRequest != null) {
            if (pageRequest.getPredicate() != null) {
                this.predicate = pageRequest.getPredicate();
            }
            if (pageRequest.getOrder() != null) {
                this.orders = Collections.singletonList(pageRequest.getOrder());
            }
        }
        
        if (pageRequest != null && pageRequest.getPage() != null) {
            return executePageQuery(pageRequest.getPage());
        } else {
            return executeListQuery();
        }
    }

    /**
     * 设置查询条件
     *
     * @param predicate 查询条件
     * @return 当前执行器实例（支持链式调用）
     */
    public QueryExecutor<E> withPredicate(QueriesPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    /**
     * 设置排序条件
     *
     * @param orders 排序条件列表
     * @return 当前执行器实例（支持链式调用）
     */
    public QueryExecutor<E> withOrders(List<QueriesOrder> orders) {
        this.orders = orders;
        return this;
    }

    /**
     * 设置单个排序条件
     *
     * @param order 排序条件
     * @return 当前执行器实例（支持链式调用）
     */
    public QueryExecutor<E> orderBy(QueriesOrder order) {
        if (this.orders == null) {
            this.orders = new ArrayList<>();
        }
        this.orders.add(order);
        return this;
    }

    /**
     * 按字段升序排序
     *
     * @param fieldName 字段名
     * @return 当前执行器实例（支持链式调用）
     */
    public QueryExecutor<E> orderByAsc(String fieldName) {
        return orderBy(new QueriesOrder(fieldName, QueriesOrder.QueriesOrderEnum.ASC));
    }

    /**
     * 按字段降序排序
     *
     * @param fieldName 字段名
     * @return 当前执行器实例（支持链式调用）
     */
    public QueryExecutor<E> orderByDesc(String fieldName) {
        return orderBy(new QueriesOrder(fieldName, QueriesOrder.QueriesOrderEnum.DESC));
    }

    /**
     * 使用完整分页请求执行分页查询
     *
     * @param pageRequest 分页请求参数（包含 predicate、order、page）
     * @return 分页结果列表
     */
    public List<E> executePageQuery(QueriesPageRequest pageRequest) {
        if (pageRequest != null) {
            if (pageRequest.getPredicate() != null) {
                this.predicate = pageRequest.getPredicate();
            }
            if (pageRequest.getOrder() != null) {
                this.orders = Collections.singletonList(pageRequest.getOrder());
            }
            if (pageRequest.getPage() != null) {
                return executePageQuery(pageRequest.getPage());
            }
        }
        return executeListQuery();
    }

    /**
     * 分页查询
     * <p>根据分页参数执行分页查询，支持排序</p>
     *
     * @param page 分页参数
     * @return 分页结果列表
     */
    public List<E> executePageQuery(QueriesPage page) {
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
     * 分页查询（便捷方法）
     *
     * @param pageNum 页码（从0开始）
     * @param pageSize 每页大小
     * @return 分页结果列表
     */
    public List<E> page(int pageNum, int pageSize) {
        return executePageQuery(new QueriesPage(pageNum, pageSize));
    }

    /**
     * 列表查询
     * <p>执行列表查询，默认返回前 10 条记录</p>
     *
     * @return 实体列表
     */
    public List<E> executeListQuery() {
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
     * 列表查询（自定义限制数量）
     *
     * @param limit 最大返回记录数
     * @return 实体列表
     */
    public List<E> list(int limit) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = buildSelectQuery(builder);
        
        applyOrdering(query, builder);
        
        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(0);
        typedQuery.setMaxResults(limit);
        
        return typedQuery.getResultList();
    }

    /**
     * 查询所有符合条件的记录（无限制）
     *
     * @return 实体列表
     */
    public List<E> listAll() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = buildSelectQuery(builder);
        
        applyOrdering(query, builder);
        
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * 计数查询
     * <p>统计符合条件的记录总数</p>
     *
     * @return 记录总数
     */
    public long executeCountQuery() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        
        // 构建计数查询
        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<E> root = countQuery.from(entityClass);
        
        // 应用查询条件
        applyWhereClause(countQuery, root, builder);
        
        // 选择计数字段
        countQuery.select(builder.count(root.get(BasicWithIdJpaEntity.ID_FIELD)));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * 检查是否存在符合条件的记录
     *
     * @return true 如果存在至少一条记录
     */
    public boolean exists() {
        return executeCountQuery() > 0;
    }

    /**
     * 查询单条记录（如果存在多条则抛出异常）
     *
     * @return 实体对象，如果不存在则返回 null
     */
    public E findOne() {
        List<E> results = list(2);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalStateException("期望查询到一条记录，但实际查询到 " + results.size() + " 条");
        }
        return results.get(0);
    }

    /**
     * 查询单条记录或抛出异常
     *
     * @return 实体对象
     * @throws NoSuchElementException 如果不存在记录
     */
    public E getOne() {
        E result = findOne();
        if (result == null) {
            throw new NoSuchElementException("未找到符合条件的记录");
        }
        return result;
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
