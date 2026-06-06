package com.vex.queries.jpa.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;
import java.util.Locale;

public class BizSnowflakeGenerator implements BeforeExecutionGenerator {

    private static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(1, 1);

    @Override
    public Object generate(SharedSessionContractImplementor session,
                           Object owner,
                           Object currentId,
                           EventType eventType) {

        // 1. 先尝试获取 @BizPrefix 注解
        BizIdPrefix bizPrefix = owner.getClass().getAnnotation(BizIdPrefix.class);

        String prefix;
        if (bizPrefix != null) {
            // 有注解 → 用注解里的前缀
            prefix = bizPrefix.value();
        } else {
            // 没注解
            String simpleName = owner.getClass().getSimpleName();

            // 不足3个字母 → 用【实体类名】
            if (simpleName.length() < 3) {
                prefix = simpleName.toLowerCase();
            } else {
                prefix = simpleName.substring(0, 3).toUpperCase(Locale.ROOT);
            }
        }

        // 2. 生成：前缀_雪花ID
        return prefix + "_" + ID_WORKER.nextId();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}