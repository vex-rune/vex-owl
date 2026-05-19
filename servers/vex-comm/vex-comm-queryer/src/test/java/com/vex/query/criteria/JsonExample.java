package com.vex.query.criteria;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON 序列化/反序列化示例
 * <p>
 * 演示如何使用 Jackson 进行 QueryCriteria 的 JSON 转换
 */
public class JsonExample {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1. 构建查询条件
        QueryCriteria query = QueryCriteria.of()
            .select("id", "username", "email")
            .filter(CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE"),
                CriteriaExpression.gte("age", 18),
                CriteriaPredicate.or(
                    CriteriaExpression.exp("username", "%张%"),
                    CriteriaExpression.exp("username", "%李%")
                )
            ))
            .orderBy(SortOrder.desc("createTime"))
            .paging(PageRequest.of(0, 20));

        // 2. 序列化为 JSON
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(query);
        System.out.println("=== 序列化结果 ===");
        System.out.println(json);

        // 3. 从 JSON 反序列化
        QueryCriteria deserialized = mapper.readValue(json, QueryCriteria.class);
        System.out.println("\n=== 反序列化成功 ===");
        System.out.println("过滤条件是否为空: " + deserialized.getFilter().checkEmpty());
    }
}

/*
 * 序列化后的 JSON 格式示例：
 * 
 * {
 *   "select": ["id", "username", "email"],
 *   "filter": {
 *     "logic": "AND",
 *     "expressions": [
 *       {
 *         "type": "expression",
 *         "field": "status",
 *         "op": "EQ",
 *         "value": "ACTIVE"
 *       },
 *       {
 *         "type": "expression",
 *         "field": "age",
 *         "op": "GTE",
 *         "value": 18
 *       },
 *       {
 *         "type": "predicate",
 *         "logic": "OR",
 *         "expressions": [
 *           {
 *             "type": "expression",
 *             "field": "username",
 *             "op": "EXP",
 *             "value": "%张%"
 *           },
 *           {
 *             "type": "expression",
 *             "field": "username",
 *             "op": "EXP",
 *             "value": "%李%"
 *           }
 *         ]
 *       }
 *     ]
 *   },
 *   "orderBy": [
 *     {
 *       "property": "createTime",
 *       "direction": "DESC"
 *     }
 *   ],
 *   "paging": {
 *     "page": 0,
 *     "size": 20
 *   }
 * }
 * 
 * 注意：
 * 1. "type" 字段由 Jackson 自动添加，用于标识具体类型
 * 2. "expression" 对应 CriteriaExpression
 * 3. "predicate" 对应 CriteriaPredicate
 * 4. 反序列化时 Jackson 会根据 "type" 自动创建正确的实例
 */
