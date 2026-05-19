package com.vex.query.criteria;

/**
 * 逻辑组合
 * <p>
 * 定义条件逻辑组合的通用行为，支持 AND、OR、NOT 三种逻辑运算
 *
 * @see Logic
 */
public enum Logic {

    /**
     * 条件与，多个条件同时满足
     */
    and,

    /**
     * 条件或，多个条件满足其一
     */
    or,

    /**
     * 条件非，对条件取反
     */
    not;

}
