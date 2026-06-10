package com.vex.security.web.filter;

import com.vex.event.TraceIdResolver;
import com.vex.security.web.RequestUserHolder;
import org.springframework.stereotype.Component;

@Component
public class TraceIdResolverImpl implements TraceIdResolver {

    @Override
    public String resolveTraceId() {
        return RequestUserHolder.getTraceId();
    }

    @Override
    public String resolveSessionId() {
        return RequestUserHolder.getSessionId();
    }

    @Override
    public String resolveUserId() {
        return RequestUserHolder.getUserId();
    }

    @Override
    public String resolveUserName() {
        return RequestUserHolder.getUserName();
    }
}
