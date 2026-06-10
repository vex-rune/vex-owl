package com.vex.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 事件元数据（不可变、JSON 友好、DDD 标准）
 * @param eventId     事件唯一ID
 * @param occurredAt  发生时间
 * @param eventType   事件类型（ORDER_CREATED、USER_UPDATED 等）
 */
public record EventMetadata(
        String eventId,
        LocalDateTime occurredAt,
        String eventType,
        CurrentTrace trace,
        CurrentUser user
) {

    public static EventMetadata of(String eventType, CurrentUser user, CurrentTrace trace) {
        return new EventMetadata(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                eventType,
                trace,
                user);
    }
}
