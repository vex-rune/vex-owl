package com.vex.query.criteria.jpa;

import com.vex.query.criteria.QueriesOperatorEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询策略工厂
 * <p>
 * 为每个操作符提供对应的策略实现，包含值类型验证和 Predicate 构建逻辑
 *
 * @author vex
 * @since 1.0.0
 */
public class QueryStrategyFactory {

    private static final Map<QueriesOperatorEnum, QueryStrategy> STRATEGY_MAP = createStrategyMap();

    private static Map<QueriesOperatorEnum, QueryStrategy> createStrategyMap() {
        return Arrays.stream(QueriesOperatorEnum.values())
            .collect(Collectors.toMap(op -> op, QueryStrategyFactory::createStrategy));
    }

    /**
     * 根据操作符获取策略
     *
     * @param operator 操作符
     * @return 对应的策略
     */
    public static QueryStrategy getStrategy(QueriesOperatorEnum operator) {
        QueryStrategy strategy = STRATEGY_MAP.get(operator);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
        return strategy;
    }

    private static QueryStrategy createStrategy(QueriesOperatorEnum op) {
        return switch (op) {
            case eq -> (cb, path, value, operator) -> cb.equal(path, value);
            case ne -> (cb, path, value, operator) -> cb.notEqual(path, value);
            case gt -> (cb, path, value, operator) -> {
                validateComparable(value, operator);
                return cb.greaterThan(path.as((Class<Comparable>) value.getClass()), (Comparable) value);
            };
            case gte -> (cb, path, value, operator) -> {
                validateComparable(value, operator);
                return cb.greaterThanOrEqualTo(path.as((Class<Comparable>) value.getClass()), (Comparable) value);
            };
            case lt -> (cb, path, value, operator) -> {
                validateComparable(value, operator);
                return cb.lessThan(path.as((Class<Comparable>) value.getClass()), (Comparable) value);
            };
            case lte -> (cb, path, value, operator) -> {
                validateComparable(value, operator);
                return cb.lessThanOrEqualTo(path.as((Class<Comparable>) value.getClass()), (Comparable) value);
            };
            case like -> (cb, path, value, operator) -> {
                validateString(value, operator);
                return cb.like(path.as(String.class), (String) value);
            };
            case exp -> (cb, path, value, operator) -> {
                validateString(value, operator);
                return cb.equal(cb.function("regexp", Boolean.class, path, cb.literal(value)), true);
            };
            case isNull -> (cb, path, value, operator) -> {
                validateNull(value, operator);
                return cb.isNull(path);
            };
            case isNotNull -> (cb, path, value, operator) -> {
                validateNull(value, operator);
                return cb.isNotNull(path);
            };
            case in -> (cb, path, value, operator) -> {
                validateIterableOrArray(value, operator);
                return buildInPredicate(path, value);
            };
        };
    }

    /**
     * 验证 Comparable 类型
     */
    private static void validateComparable(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " operator requires a non-null Comparable value");
        }
        if (!(value instanceof Comparable)) {
            throw new IllegalArgumentException(operator + " operator requires a Comparable value, got: " +
                value.getClass().getName());
        }
    }

    /**
     * 验证 String 类型
     */
    private static void validateString(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " operator requires a non-null String value");
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(operator + " operator requires a String value, got: " +
                value.getClass().getName());
        }
    }

    /**
     * 验证值为 null
     */
    private static void validateNull(Object value, QueriesOperatorEnum operator) {
        if (value != null) {
            throw new IllegalArgumentException(operator + " operator does not require a value");
        }
    }

    /**
     * 验证 Iterable 或数组类型
     */
    private static void validateIterableOrArray(Object value, QueriesOperatorEnum operator) {
        if (value == null) {
            throw new IllegalArgumentException(operator + " operator requires a non-null Iterable or array value");
        }
        if (!(value instanceof Iterable) && !value.getClass().isArray()) {
            throw new IllegalArgumentException(operator + " operator requires an Iterable or array value, got: " +
                value.getClass().getName());
        }
    }

    /**
     * 构建 IN Predicate
     */
    @SuppressWarnings("unchecked")
    private static Predicate buildInPredicate(Path<?> path, Object value) {
        if (value instanceof Iterable) {
            return path.in((Iterable<?>) value);
        } else if (value.getClass().isArray()) {
            if (value instanceof Object[]) {
                return path.in(Arrays.asList((Object[]) value));
            } else if (value instanceof int[]) {
                return path.in(Arrays.stream((int[]) value).boxed().toList());
            } else if (value instanceof long[]) {
                return path.in(Arrays.stream((long[]) value).boxed().toList());
            } else if (value instanceof double[]) {
                return path.in(Arrays.stream((double[]) value).boxed().toList());
            }
        }
        throw new IllegalArgumentException("IN operator requires an Iterable or array value");
    }
}
