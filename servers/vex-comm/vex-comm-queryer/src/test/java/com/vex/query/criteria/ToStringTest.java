package com.vex.query.criteria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * toString 方法测试
 */
@DisplayName("toString 方法测试")
class ToStringTest {

    @Test
    @DisplayName("CriteriaExpression toString")
    void testCriteriaExpressionToString() {
        CriteriaExpression expr = CriteriaExpression.eq("status", "ACTIVE");
        System.out.println("表达式: " + expr);
        // 输出: status EQ ACTIVE
        
        CriteriaExpression betweenExpr = CriteriaExpression.between("age", 18, 65);
        System.out.println("范围表达式: " + betweenExpr);
        // 输出: age BETWEEN [18, 65]
        
        CriteriaExpression nullExpr = CriteriaExpression.isNull("deletedAt");
        System.out.println("NULL表达式: " + nullExpr);
        // 输出: deletedAt IS_NULL NULL
    }

    @Test
    @DisplayName("CriteriaPredicate toString")
    void testCriteriaPredicateToString() {
        CriteriaPredicate predicate = CriteriaPredicate.and(
            CriteriaExpression.eq("status", "ACTIVE"),
            CriteriaExpression.gte("age", 18)
        );
        System.out.println("AND条件: " + predicate);
        // 输出: ((status EQ ACTIVE) AND (age GTE 18))
        
        CriteriaPredicate orPredicate = CriteriaPredicate.or(
            CriteriaExpression.exp("username", "%张%"),
            CriteriaExpression.exp("username", "%李%")
        );
        System.out.println("OR条件: " + orPredicate);
        // 输出: ((username EXP %张%) OR (username EXP %李%))
    }

    @Test
    @DisplayName("嵌套条件 toString")
    void testNestedPredicateToString() {
        CriteriaPredicate nested = CriteriaPredicate.and(
            CriteriaExpression.eq("status", "ACTIVE"),
            CriteriaPredicate.or(
                CriteriaExpression.exp("username", "%张%"),
                CriteriaExpression.exp("username", "%李%")
            )
        );
        System.out.println("嵌套条件: " + nested);
        // 输出: ((status EQ ACTIVE) AND ((username EXP %张%) OR (username EXP %李%)))
    }

    @Test
    @DisplayName("SortOrder toString")
    void testSortOrderToString() {
        SortOrder asc = SortOrder.asc("createTime");
        System.out.println("升序: " + asc);
        // 输出: createTime ASC
        
        SortOrder desc = SortOrder.desc("updateTime");
        System.out.println("降序: " + desc);
        // 输出: updateTime DESC
    }

    @Test
    @DisplayName("PageRequest toString")
    void testPageRequestToString() {
        PageRequest paging = PageRequest.of(0, 20);
        System.out.println("分页: " + paging);
        // 输出: page=0, size=20
    }

    @Test
    @DisplayName("QueryCriteria toString")
    void testQueryCriteriaToString() {
        QueryCriteria query = QueryCriteria.of()
            .select("id", "username", "email")
            .exclude("password")
            .filter(CriteriaPredicate.and(
                CriteriaExpression.eq("status", "ACTIVE")
            ))
            .orderBy(SortOrder.desc("createTime"))
            .paging(PageRequest.of(0, 20));
        
        System.out.println("完整查询: " + query);
        // 输出: QueryCriteria{select=[id, username, email], exclude=[password], filter=((status EQ ACTIVE)), orderBy=[updateTime DESC], paging=page=0, size=20}
    }

    @Test
    @DisplayName("空查询 toString")
    void testEmptyQueryToString() {
        QueryCriteria query = QueryCriteria.of();
        System.out.println("空查询: " + query);
        // 输出: QueryCriteria{}
    }
}
