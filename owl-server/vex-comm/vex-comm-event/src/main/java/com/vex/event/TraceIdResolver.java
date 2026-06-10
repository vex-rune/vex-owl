package com.vex.event;

/**
 * 请求上下文解析器（由外部模块实现）
 *
 * <p>event 模块不依赖 web/auth，通过此接口获取当前请求的链路信息。</p>
 */
public interface TraceIdResolver {

    String resolveTraceId();

    String resolveSessionId();

    String resolveUserId();

    String resolveUserName();

    static TraceIdResolver none() {
        return new TraceIdResolver() {
            @Override public String resolveTraceId() { return ""; }
            @Override public String resolveSessionId() { return ""; }
            @Override public String resolveUserId() { return ""; }
            @Override public String resolveUserName() { return ""; }
        };
    }
}
