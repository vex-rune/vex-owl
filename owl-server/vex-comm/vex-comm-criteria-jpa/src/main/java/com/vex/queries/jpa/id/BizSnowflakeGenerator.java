package com.vex.queries.jpa.id;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.Locale;

@Slf4j
public class BizSnowflakeGenerator implements BeforeExecutionGenerator {

    private static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(1, 1);

    @Override
    public Object generate(SharedSessionContractImplementor session,
                           Object owner,
                           Object currentId,
                           EventType eventType) {

        // 1. 先尝试获取 @BizPrefix 注解
        BizSnowId bizPrefix = owner.getClass().getAnnotation(BizSnowId.class);

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
        return generateId(owner);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    public String generateId(Object entity) {
        try {
            Field idField = findIdField(entity.getClass());
            if (idField == null) return null;

            BizSnowId annotation = idField.getAnnotation(BizSnowId.class);
            if (annotation == null) return null;

            idField.setAccessible(true);
            Object currentValue = idField.get(entity);
            if (currentValue != null) return null;

            String prefix = resolvePrefix(entity);
            String id = prefix + "_" + ID_WORKER.nextId();
            log.debug("Generated ID: {}", id);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("BizIdListener: ID 生成失败", e);
        }
    }

    private Field findIdField(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(BizSnowId.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private String resolvePrefix(Object entity) {
        BizSnowId bizPrefix = entity.getClass().getAnnotation(BizSnowId.class);
        if (bizPrefix != null) {
            return bizPrefix.value();
        }

        String simpleName = entity.getClass().getSimpleName();
        if (simpleName.length() < 3) {
            return simpleName.toLowerCase(Locale.ROOT);
        }
        return simpleName.substring(0, 3).toUpperCase(Locale.ROOT);
    }
}