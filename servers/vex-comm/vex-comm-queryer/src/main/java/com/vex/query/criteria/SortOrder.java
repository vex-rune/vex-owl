package com.vex.query.criteria;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排序规则
 * <p>
 * 用于指定查询结果的排序方式
 * <p>
 * 示例：
 * <pre>
 *     SortOrder.asc("createTime")
 *     SortOrder.desc("username")
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SortOrder {
    /**
     * 排序字段
     */
    private String property;

    /**
     * 排序方向，默认 ASC
     */
    private String direction = "ASC";

    /**
     * 判断是否为降序排序
     *
     * @return true 表示降序（DESC），false 表示升序（ASC）
     */
    public boolean checkDesc() {
        return "DESC".equals(direction);
    }

    /**
     * 创建默认排序规则（升序）
     *
     * @param property 排序字段名
     * @return SortOrder 实例，默认按升序排列
     */
    public static SortOrder of(String property) {
        SortOrder s = new SortOrder();
        s.property = property;
        s.direction = "ASC";
        return s;
    }

    /**
     * 创建自定义排序规则
     *
     * @param property  排序字段名
     * @param direction 排序方向，"ASC" 表示升序，"DESC" 表示降序
     * @return SortOrder 实例
     */
    public static SortOrder of(String property, String direction) {
        SortOrder s = new SortOrder();
        s.property = property;
        s.direction = direction;
        return s;
    }

    /**
     * 创建升序排序规则
     *
     * @param property 排序字段名
     * @return SortOrder 实例，按升序排列
     */
    public static SortOrder asc(String property) {
        return of(property, "ASC");
    }

    /**
     * 创建降序排序规则
     *
     * @param property 排序字段名
     * @return SortOrder 实例，按降序排列
     */
    public static SortOrder desc(String property) {
        return of(property, "DESC");
    }

    @Override
    public String toString() {
        return property + " " + direction;
    }
}
