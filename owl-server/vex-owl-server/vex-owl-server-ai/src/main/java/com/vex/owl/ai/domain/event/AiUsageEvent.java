package com.vex.owl.ai.domain.event;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * AI 使用事件统一抽象
 *
 * <p>所有 AI 相关的使用事件都应该继承此类</p>
 *
 * <h2>设计原则</h2>
 * <ul>
 *   <li>必须包含 AiContextMetadata，用于追踪调用上下文</li>
 *   <li>泛型 data 字段用于存储业务相关数据</li>
 *   <li>统一的时间戳字段</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 定义具体事件
 * public record TokenUsageData(int promptTokens, int completionTokens, int totalTokens) {}
 *
 * public record TokenUsageEvent(
 *     AiContextMetadata metadata,
 *     TokenUsageData data,
 *     Instant timestamp
 * ) {}
 *
 * // 发布事件
 * TokenUsageEvent event = new TokenUsageEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("speech-2.8-hd")
 *         .aiType(AiContextMetadata.AiType.VOICE)
 *         .build(),
 *     new TokenUsageData(25, 3500, 56000),
 *     Instant.now()
 * );
 *
 * // 从 Map 转换
 * Map<String, Object> context = event.getMetadata().toMap();
 * AiContextMetadata metadata = AiContextMetadata.fromMap(context);
 * }</pre>
 *
 * @param <T> 业务数据类型
 */
@Getter
public abstract class AiUsageEvent<T> {

    /**
     * AI 上下文元数据
     * 包含平台、模型、类型、租户、会话等信息
     */
    private final AiContextMetadata metadata;

    /**
     * 业务相关数据
     * 具体类型由子类定义
     */
    private final T data;

    /**
     * 事件发生时间戳
     */
    private final Instant timestamp;

    /**
     * 事件类型标识
     * 子类应覆盖此方法返回唯一标识
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }

    protected AiUsageEvent(AiContextMetadata metadata, T data, Instant timestamp) {
        this.metadata = metadata;
        this.data = data;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    /**
     * 从 Map 创建 AiUsageEvent
     *
     * <p>从事件的 context Map 中提取元数据和业务数据</p>
     *
     * @param context 包含元数据和业务数据的 Map
     * @param dataClass 业务数据类型 Class
     * @return AiUsageEvent 实例
     */
    public static <T, E extends AiUsageEvent<T>> E fromContext(
            Map<String, Object> context,
            Class<E> eventClass
    ) {
        if (context == null) {
            return null;
        }

        AiContextMetadata metadata = AiContextMetadata.fromMap(context);

        try {
            return eventClass.getDeclaredConstructor(
                    AiContextMetadata.class,
                    Object.class,
                    Instant.class
            ).newInstance(metadata, null, Instant.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create event from context", e);
        }
    }

    /**
     * 转换为 Map
     *
     * <p>将元数据转换为 Map，方便存储和传输</p>
     *
     * @return 包含元数据的 Map
     */
    public Map<String, Object> toContextMap() {
        Map<String, Object> map = new java.util.HashMap<>();

        if (metadata != null) {
            map.putAll(metadata.toMap());
        }

        return map;
    }
}
