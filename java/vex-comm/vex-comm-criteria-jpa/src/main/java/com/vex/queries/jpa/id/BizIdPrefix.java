package com.vex.queries.jpa.id;

import java.lang.annotation.*;

/**
 * 业务前缀注解
 * 实体上直接写：@BizPrefix("USER") / @BizPrefix("ORDER")
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BizIdPrefix {
    String value();
}