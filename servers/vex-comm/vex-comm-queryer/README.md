# Queryer 通用查询框架

## 概述

查询条件的数据模型，参考 JPA Criteria API 命名，使用 Lombok 简化代码。

---

## 快速构建

```java
// 简单条件
Expression.eq("status", "ACTIVE")
Expression.like("username", "john")
Expression.between("age", 18, 65)

// 条件组合
Predicate.and(
    Expression.eq("status", "ACTIVE"),
    Expression.gte("age", 18)
)

Predicate.or(
    Expression.eq("status", "PENDING"),
    Expression.eq("status", "ACTIVE")
)

// 链式构建查询
Criteria.of()
    .select("id", "username", "email")
    .filter(Predicate.and(
        Expression.like("username", "john"),
        Expression.in("status", new String[]{"ACTIVE", "PENDING"})
    ))
    .orderBy(Sort.desc("createTime"))
    .paging(Pageable.of(0, 20))
```

---

## 查询结构

```json
{
  "select": ["id", "username", "email"],
  "exclude": ["password", "salt"],
  "filter": {
    "logic": "AND",
    "exprs": [
      {"field": "username", "op": "LIKE", "value": "john"},
      {
        "logic": "OR",
        "exprs": [
          {"field": "status", "op": "EQ", "value": "ACTIVE"},
          {"field": "status", "op": "EQ", "value": "PENDING"}
        ]
      }
    ]
  },
  "orderBy": [
    {"property": "createTime", "direction": "DESC"}
  ],
  "paging": {
    "num": 0,
    "size": 20
  }
}
```

---

## 模块结构

```
queryer/
├── Logic.java      // 逻辑组合
├── Operator.java   // 操作符枚举
├── Criteria.java   // 查询条件（链式构建）
├── Expression.java // 表达式（快速构建）
├── Predicate.java  // 谓词（条件组合）
├── Sort.java       // 排序
├── Pageable.java   // 分页请求
└── Page.java       // 分页结果
```

---

## 操作符 Operator

| 枚举 | 说明 |
|------|------|
| `EQ` | 等于 |
| `NEQ` | 不等于 |
| `GT` | 大于 |
| `GTE` | 大于等于 |
| `LT` | 小于 |
| `LTE` | 小于等于 |
| `LIKE` | 模糊匹配 |
| `NOT_LIKE` | 模糊匹配（取反） |
| `IN` | 在集合中 |
| `NOT_IN` | 不在集合中 |
| `BETWEEN` | 范围 |
| `IS_NULL` | 为空 |
| `IS_NOT_NULL` | 不为空 |

---

## 逻辑组合 Logic

| 枚举 | 说明 |
|------|------|
| `AND` | 条件与 |
| `OR` | 条件或 |
| `NOT` | 条件非 |
