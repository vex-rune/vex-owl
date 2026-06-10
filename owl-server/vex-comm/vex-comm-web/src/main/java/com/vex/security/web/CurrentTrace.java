package com.vex.security.web;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前追踪上下文（TraceIdFilter 写入）
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