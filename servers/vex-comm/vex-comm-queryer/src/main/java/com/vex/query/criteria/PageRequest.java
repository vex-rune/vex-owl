package com.vex.query.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求
 * <p>
 * 用于指定查询结果的分页信息
 * <p>
 * 示例：
 * <pre>
 *     PageRequest.of(0, 20)  // 第1页，每页20条
 *     PageRequest.first()     // 第1页，每页20条
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    /**
     * 页码，从 0 开始
     */
    private int page = 0;

    /**
     * 每页条数，默认 20
     */
    private int size = 20;

    /**
     * 创建分页对象
     *
     * @param page 页码，从 0 开始（0 表示第一页）
     * @param size 每页条数
     * @return PageRequest 实例
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    @Override
    public String toString() {
        return String.format("page=%d, size=%d", page, size);
    }

}
