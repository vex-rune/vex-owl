# JPA Criteria 查询构建器

## 📖 概述

本模块提供了基于 JPA Criteria API 的动态查询构建功能，支持类型安全的查询条件构建、字段选择和排序。

## ✨ 主要特性

- ✅ **类型安全**：在编译期进行严格的类型校验，避免运行时错误
- ✅ **动态查询**：支持运行时动态构建查询条件
- ✅ **灵活过滤**：支持 13 种操作符（eq, neq, gt, gte, lt, lte, exp, not_exp, in, not_in, between, is_null, is_not_null）
- ✅ **字段选择**：支持包含（select）和排除（exclude）字段
- ✅ **多字段排序**：支持升序/降序组合排序
- ✅ **嵌套条件**：支持 AND/OR/NOT 逻辑组合

## 🚀 快速开始

### 1. 创建查询条件

```java
// 简单查询：age > 18 AND name LIKE '%John%'
VexQueryCriteria criteria = VexQueryCriteria.builder()
    .filter(VexPredicate.and(
        VexExpression.gt("age", 18),
        VexExpression.exp("name", "%John%")
    ))
    .orderBy(VexSortOrder.desc("createTime"))
    .build();
```

### 2. 构建并执行查询

```java
// 构建 JPA CriteriaQuery
CriteriaQuery<User> query = CriteriaQueryJpaBuilder.buildQuery(
    User.class,
    criteria,
    entityManager.getCriteriaBuilder()
);

// 执行查询
List<User> users = entityManager.createQuery(query).getResultList();
```

## 📋 操作符说明

| 操作符 | 说明 | 值类型要求 | 示例 |
|--------|------|-----------|------|
| `eq` | 相等 | 任意类型（可为 null） | `VexExpression.eq("status", "active")` |
| `neq` | 不相等 | 任意类型（可为 null） | `VexExpression.neq("status", "deleted")` |
| `gt` | 大于 | Comparable | `VexExpression.gt("age", 18)` |
| `gte` | 大于等于 | Comparable | `VexExpression.gte("score", 60)` |
| `lt` | 小于 | Comparable | `VexExpression.lt("price", 100)` |
| `lte` | 小于等于 | Comparable | `VexExpression.lte("quantity", 10)` |
| `exp` | 模糊匹配（LIKE） | String | `VexExpression.exp("name", "%John%")` |
| `not_exp` | 不匹配（NOT LIKE） | String | `VexExpression.notExp("email", "%spam%")` |
| `in` | 在集合中 | Iterable | `VexExpression.in("id", List.of(1,2,3))` |
| `not_in` | 不在集合中 | Iterable | `VexExpression.notIn("status", Set.of("inactive"))` |
| `between` | 范围查询 | Object[2] | `VexExpression.between("age", 18, 60)` |
| `is_null` | 为空 | null | `VexExpression.isNull("deletedAt")` |
| `is_not_null` | 不为空 | null | `VexExpression.isNotNull("createdAt")` |

## 🔍 高级用法

### 复杂条件组合

```java
// (age > 18 AND status = 'active') OR (role IN ['admin', 'manager'])
VexPredicate complexFilter = VexPredicate.or(
    VexPredicate.and(
        VexExpression.gt("age", 18),
        VexExpression.eq("status", "active")
    ),
    VexExpression.in("role", List.of("admin", "manager"))
);

VexQueryCriteria criteria = VexQueryCriteria.builder()
    .filter(complexFilter)
    .build();
```

### 字段选择

```java
// 只查询 id 和 name 字段
VexQueryCriteria criteria = VexQueryCriteria.builder()
    .select("id", "name")
    .build();

// 查询所有字段，排除 password
VexQueryCriteria criteria = VexQueryCriteria.builder()
    .exclude("password")
    .build();
```

### 多字段排序

```java
// 先按 createTime 降序，再按 name 升序
VexQueryCriteria criteria = VexQueryCriteria.builder()
    .orderBy(
        VexSortOrder.desc("createTime"),
        VexSortOrder.asc("name")
    )
    .build();
```

## 🛡️ 类型校验

本模块在构建查询时会进行严格的类型校验：

```java
// ✅ 正确：Integer 实现 Comparable
VexExpression.gt("age", 18);

// ❌ 错误：会抛出 IllegalArgumentException
VexExpression.gt("name", "abc");  // String 虽然实现 Comparable，但语义不合理
VexExpression.exp("age", 123);    // exp 需要 String 类型
VexExpression.in("id", "1,2,3");  // in 需要 Iterable 类型
```

## 📝 错误处理

所有校验失败都会抛出清晰的异常信息：

```
Field 'invalidField' does not exist in entity 'User'
gt operator requires a Comparable value, got: java.lang.String
BETWEEN operator requires exactly 2 values, got: 3
```

## 🧪 单元测试

运行测试：

```bash
mvn test -Dtest=VexExpressionJpaValidatorTest
```

测试覆盖：
- ✅ 字段名校验（null、空字符串、空白字符串）
- ✅ 所有操作符的类型校验
- ✅ 边界情况（null 值、空集合等）
- ✅ 错误消息的准确性

## 📦 项目结构

```
vex-comm-criteria-jpa/
├── src/main/java/com/vex/query/criteria/jpa/
│   ├── CriteriaQueryJpaBuilder.java      # 查询构建器
│   └── VexExpressionJpaValidator.java    # 表达式校验器
└── src/test/java/com/vex/query/criteria/jpa/
    └── VexExpressionJpaValidatorTest.java # 单元测试
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License
