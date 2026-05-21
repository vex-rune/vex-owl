package com.vex.queries.jpa.id;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.*;

/**
 * 最终复合注解：@BizSnowId
 * 一键启用：前缀 + 雪花ID
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@IdGeneratorType(BizSnowflakeGenerator.class) // 绑定你的生成器！
public @interface BizSnowId {
}