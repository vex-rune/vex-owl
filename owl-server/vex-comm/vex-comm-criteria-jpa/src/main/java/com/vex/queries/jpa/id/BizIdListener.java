package com.vex.queries.jpa.id;

import jakarta.persistence.PrePersist;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * JPA 实体 ID 自动生成监听器
 *
 * <p>在 {@code @PrePersist} 阶段为带有 {@link BizSnowId} 注解的字段自动生成 ID。
 * 格式：{@code 前缀_雪花ID}，前缀取自 {@link BizIdPrefix} 注解或实体类名前3位。</p>
 *
 * <p>替代 {@link BizSnowflakeGenerator}（{@code BeforeExecutionGenerator}），
 * 解决 Spring Data JPA {@code save()/persist()} 不触发 ID 生成的问题。</p>
 */
public class BizIdListener {

    private static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(1, 1);

    @PrePersist
    public void generateId(Object entity) {
        try {
            Field idField = findIdField(entity.getClass());
            if (idField == null) return;

            BizSnowId annotation = idField.getAnnotation(BizSnowId.class);
            if (annotation == null) return;

            idField.setAccessible(true);
            Object currentValue = idField.get(entity);
            if (currentValue != null) return; // 已有 ID，不覆盖

            String prefix = resolvePrefix(entity);
            String id = prefix + "_" + ID_WORKER.nextId();
            idField.set(entity, id);
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
