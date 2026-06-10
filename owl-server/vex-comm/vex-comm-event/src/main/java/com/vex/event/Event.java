package com.vex.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Event<P extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = 48756L;

    /**
     * 获取元数据
     */
    EventMetadata metadata;

    /**
     * 获取业务数据（变更数据）
     */
    P payload;


    public static <T extends Serializable> Event<T> of(EventMetadata metadata, T payload) {
        Event<T> event = new Event<>();
        event.setMetadata(metadata);
        event.setPayload(payload);
        return event;
    }
}
