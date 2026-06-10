package com.vex.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前追踪上下文（event 标准模型）
 *
 * <p>存储链路追踪信息，由 TraceIdFilter 写入 Reactor Context。</p>
 */
@Data
@Builder
public class CurrentTrace implements Serializable {

    private String sessionId;
    private String traceId;

    public static CurrentTrace anonymous() {
        return CurrentTrace.builder().sessionId("").traceId("").build();
    }
}