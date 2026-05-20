# Vex Criteria 通用查询框架

## 概述

查询条件的数据模型，参考 JPA Criteria API 命名，使用 Lombok 简化代码。

---

## 快速构建

```java
// 简单条件
VexExpression.eq("status", "ACTIVE")
VexExpression.exp("username", "john")
VexExpression.between("age", 18, 65)

// 条件组合
VexPredicate.and(
    VexExpression.eq("status", "ACTIVE"),
    VexExpression.gte("age", 18)
)

VexPredicate.or(
    VexExpression.eq("status", "PENDING"),
    VexExpression.eq("status", "ACTIVE")
)

// 链式构建查询
VexQueryCriteria.of()
    .select("id", "username", "email")
    .filter(VexPredicate.and(
        VexExpression.exp("username", "john"),
        VexExpression.in("status", new String[]{"ACTIVE", "PENDING"})
    ))
    .orderBy(VexSortOrder.desc("createTime"))
    .paging(VexPageRequest.of(0, 20))
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
      {"field": "username", "op": "exp", "value": "john"},
      {
        "logic": "OR",
        "exprs": [
          {"field": "status", "op": "eq", "value": "ACTIVE"},
          {"field": "status", "op": "eq", "value": "PENDING"}
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
criteria/
├── VexLogic.java       // 逻辑组合
├── VexOperator.java    // 操作符枚举
├── VexQueryCriteria.java   // 查询条件（链式构建）
├── VexExpression.java  // 表达式（快速构建）
├── VexPredicate.java   // 谓词（条件组合）
├── VexSortOrder.java   // 排序
├── VexPageRequest.java // 分页请求
└── VexExpressionValidator.java  // 表达式校验
```

---

## 操作符 VexOperator

| 枚举 | 说明 |
|------|------|
| `eq` | 等于 |
| `neq` | 不等于 |
| `gt` | 大于 |
| `gte` | 大于等于 |
| `lt` | 小于 |
| `lte` | 小于等于 |
| `exp` | 模糊匹配 |
| `not_exp` | 模糊匹配（取反） |
| `in` | 在集合中 |
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
