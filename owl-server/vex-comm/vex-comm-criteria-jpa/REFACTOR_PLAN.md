# 删除三个类并保留逻辑的重构方案

## 🎯 目标

删除以下三个文件，但保留它们的逻辑：
- ❌ `CriteriaQueryEngine.java`
- ❌ `PredicateStrategy.java`  
- ❌ `PredicateStrategyFactory.java`

## ✅ 解决方案

将所有逻辑**内联合并**到 `QueryExecutor.java` 中。

## 📋 重构步骤

### 1. 已完成的删除操作
✅ 已删除 `CriteriaQueryEngine.java`
✅ 已删除 `PredicateStrategy.java`
✅ 已删除 `PredicateStrategyFactory.java`

### 2. QueryExecutor 需要包含的内容

#### A. 策略接口（原 PredicateStrategy）
```java
@FunctionalInterface
private interface PredicateBuilder {
    Predicate build(CriteriaBuilder builder, Path<?> path, Object value, QueriesOperatorEnum operator);
}
```

或者使用 Java 16+ Record + Function:
```java
private record PredicateContext(CriteriaBuilder builder, Path<?> path, Object value, QueriesOperatorEnum operator) {}
private static final Map<QueriesOperatorEnum, Function<PredicateContext, Predicate>> STRATEGY_CACHE = new ConcurrentHashMap<>();
```

#### B. 策略工厂逻辑（原 PredicateStrategyFactory）
```java
static {
    initializeStrategies();
}

private static void initializeStrategies() {
    Arrays.stream(QueriesOperatorEnum.values())
            .forEach(operator -> STRATEGY_CACHE.put(operator, createStrategy(operator)));
}

private static Function<PredicateContext, Predicate> createStrategy(QueriesOperatorEnum operator) {
    return switch (operator) {
        case eq -> ctx -> ctx.builder().equal(ctx.path(), ctx.value());
        case ne -> ctx -> ctx.builder().notEqual(ctx.path(), ctx.value());
        case gt -> ctx -> { 
            validateComparable(ctx.value(), ctx.operator()); 
            return ctx.builder().greaterThan(...); 
        };
        // ... 其他操作符
    };
}

// 验证方法
private static void validateComparable(Object value, QueriesOperatorEnum operator) { ... }
private static void validateString(Object value, QueriesOperatorEnum operator) { ... }
private static void validateNull(Object value, QueriesOperatorEnum operator) { ... }
private static void validateCollection(Object value, QueriesOperatorEnum operator) { ... }
private static Predicate buildInPredicate(Path<?> path, Object value) { ... }
```

#### C. 查询引擎逻辑（原 CriteriaQueryEngine）
```java
private CriteriaQuery<E> buildSelectQuery(CriteriaBuilder builder) {
    CriteriaQuery<E> query = builder.createQuery(entityClass);
    Root<E> root = query.from(entityClass);
    applyWhereClause(query, root, builder);
    return query;
}

private void applyWhereClause(CriteriaQuery<?> query, Root<?> root, CriteriaBuilder builder) {
    if (isEmptyPredicate(predicate)) {
        query.where();
        return;
    }
    List<Predicate> predicates = new ArrayList<>();
    if (predicate.getAnd() != null && !predicate.getAnd().isEmpty()) {
        predicates.add(buildPredicateFromCriteria(predicate.getAnd(), true, root, builder));
    }
    if (predicate.getOr() != null && !predicate.getOr().isEmpty()) {
        predicates.add(buildPredicateFromCriteria(predicate.getOr(), false, root, builder));
    }
    // 组合 predicates...
}

private Predicate buildPredicateFromCriteria(List<QueriesCriteria> criteriaList, boolean isAnd, Root<?> root, CriteriaBuilder builder) {
    // 构建 Predicate 列表
}

private Predicate buildPredicateFromCondition(QueriesCondition condition, Root<?> root, CriteriaBuilder builder) {
    Path<Object> path = root.get(condition.getField());
    Function<PredicateContext, Predicate> strategy = STRATEGY_CACHE.get(condition.getOp());
    return strategy.apply(new PredicateContext(builder, path, condition.getValue(), condition.getOp()));
}
```

## 🔧 关键改动点

### 1. 替换 CriteriaQueryEngine.buildQuery() 调用
**原来：**
```java
private CriteriaQuery<E> buildSelectQuery(CriteriaBuilder builder) {
    return CriteriaQueryEngine.buildQuery(entityClass, predicate, builder);
}
```

**改为：**
```java
private CriteriaQuery<E> buildSelectQuery(CriteriaBuilder builder) {
    CriteriaQuery<E> query = builder.createQuery(entityClass);
    Root<E> root = query.from(entityClass);
    applyWhereClause(query, root, builder);
    return query;
}
```

### 2. 替换 PredicateStrategyFactory.getStrategy() 调用
**原来：**
```java
PredicateStrategy strategy = PredicateStrategyFactory.getStrategy(operator);
return strategy.build(builder, path, value, operator);
```

**改为：**
```java
Function<PredicateContext, Predicate> strategy = STRATEGY_CACHE.get(operator);
return strategy.apply(new PredicateContext(builder, path, value, operator));
```

## 📦 最终文件结构

```
jpa/
├── JpaEntity.java                  # ✅ 保留
├── IdentifiableEntity.java         # ✅ 保留
└── QueryExecutor.java              # ✅ 保留（整合所有逻辑）
```

## ⚠️ 注意事项

1. **IDE 缓存问题**：如果看到编译错误但文件内容正确，尝试：
   - File → Invalidate Caches / Restart
   - 或删除 `.idea` 文件夹后重新打开项目

2. **导入语句**：确保添加必要的导入：
   ```java
   import java.util.concurrent.ConcurrentHashMap;
   import java.util.function.Function;
   ```

3. **Java 版本**：Record 需要 Java 16+，如果使用低版本，改用内部接口

4. **线程安全**：STRATEGY_CACHE 使用 ConcurrentHashMap 保证线程安全

## ✨ 优势

1. **简化架构**：从 6 个类减少到 3 个类
2. **减少抽象层**：去除不必要的接口和工厂
3. **提升性能**：减少方法调用层次
4. **易于理解**：所有逻辑在一个类中，更容易追踪

## 🚀 使用方式不变

```java
// 使用方式完全相同
new QueryExecutor<>(User.class, entityManager)
    .withPredicate(predicate)
    .withOrders(orders)
    .executePageQuery(pageRequest);
```

---

**完成日期**: 2026-05-20  
**状态**: 🔄 进行中（需手动更新 QueryExecutor.java）
