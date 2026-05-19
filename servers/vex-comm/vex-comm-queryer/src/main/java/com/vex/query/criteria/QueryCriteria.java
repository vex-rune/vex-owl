package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 查询条件
 * <p>
 * 用于构建复杂的查询条件，支持字段选择、条件过滤、排序和分页
 * <p>
 * 示例：
 * <pre>
 *     QueryCriteria.of()
 *         .select("id", "username", "email")
 *         .exclude("password", "salt")
 *         .filter(CriteriaPredicate.and(
 *             CriteriaExpression.exp("username", "john"),
 *             CriteriaExpression.in("status", new String[]{"ACTIVE", "PENDING"})
 *         ))
 *         .orderBy(SortOrder.desc("createTime"))
 *         .paging(PageRequest.of(0, 20))
 * </pre>
 *
 * @see CriteriaExpression
 * @see CriteriaPredicate
 * @see SortOrder
 * @see PageRequest
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryCriteria {
    /**
     * 要查询的字段列表
     */
    private String[] select;

    /**
     * 要排除的字段列表
     */
    private String[] exclude;

    /**
     * 查询条件组合
     */
    private CriteriaPredicate filter;

    /**
     * 排序规则
     */
    private SortOrder[] orderBy;

    /**
     * 分页信息
     */
    private PageRequest paging;

    /**
     * 设置要查询的字段列表
     *
     * @param fields 字段名数组，指定需要返回的字段
     * @return 当前 QueryCriteria 实例，支持链式调用
     */
    public QueryCriteria select(String... fields) {
        this.select = fields;
        return this;
    }

    /**
     * 设置要排除的字段列表
     *
     * @param fields 字段名数组，指定不需要返回的字段
     * @return 当前 QueryCriteria 实例，支持链式调用
     */
    public QueryCriteria exclude(String... fields) {
        this.exclude = fields;
        return this;
    }

    /**
     * 设置查询条件过滤器
     *
     * @param filter 查询条件谓词，用于过滤数据
     * @return 当前 QueryCriteria 实例，支持链式调用
     */
    public QueryCriteria filter(CriteriaPredicate filter) {
        this.filter = filter;
        return this;
    }

    /**
     * 设置排序规则
     *
     * @param sorts 排序规则数组，可指定多个字段的排序方式
     * @return 当前 QueryCriteria 实例，支持链式调用
     */
    public QueryCriteria orderBy(SortOrder... sorts) {
        this.orderBy = sorts;
        return this;
    }

    /**
     * 设置分页信息
     *
     * @param paging 分页对象，包含页码和每页条数
     * @return 当前 QueryCriteria 实例，支持链式调用
     */
    public QueryCriteria paging(PageRequest paging) {
        this.paging = paging;
        return this;
    }

    /**
     * 创建空的查询对象
     *
     * @return 新的 QueryCriteria 实例
     */
    public static QueryCriteria of() {
        return new QueryCriteria();
    }

    /**
     * 创建带字段选择的查询对象
     *
     * @param fields 要查询的字段名数组
     * @return 新的 QueryCriteria 实例，已设置 select 字段
     */
    public static QueryCriteria query(String... fields) {
        QueryCriteria q = new QueryCriteria();
        q.select = fields;
        return q;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("QueryCriteria{");
        
        if (select != null && select.length > 0) {
            sb.append("select=").append(java.util.Arrays.toString(select));
        }
        
        if (exclude != null && exclude.length > 0) {
            if (sb.length() > 13) sb.append(", ");
            sb.append("exclude=").append(java.util.Arrays.toString(exclude));
        }
        
        if (filter != null) {
            if (sb.length() > 13) sb.append(", ");
            sb.append("filter=").append(filter);
        }
        
        if (orderBy != null && orderBy.length > 0) {
            if (sb.length() > 13) sb.append(", ");
            sb.append("orderBy=").append(java.util.Arrays.toString(orderBy));
        }
        
        if (paging != null) {
            if (sb.length() > 13) sb.append(", ");
            sb.append("paging=").append(paging);
        }
        
        sb.append("}");
        return sb.toString();
    }
}
