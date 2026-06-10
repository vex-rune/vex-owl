package com.vex.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户上下文（event 标准模型）
 *
 * <p>存储请求级用户信息，由 AuthUserFilter 写入 Reactor Context。
 * TraceIdFilter 写入的 CurrentTrace 通过 AuthUserFilter 合并到此对象中。</p>
 */
@Data
@Builder
public class CurrentUser implements Serializable {

    private boolean authEnabled;
    private String userId;
    private String userName;
    private String userGroup;
    private String loginTime;
    private String role;
    private String email;
    private String nickname;
    private String sessionId;
    private String traceId;

    public static CurrentUser anonymous() {
        return CurrentUser.builder()
                .authEnabled(false)
                .userId("")
                .userName("")
                .userGroup("default")
                .loginTime("0")
                .role("")
                .email("")
                .nickname("")
                .sessionId("")
                .traceId("")
                .build();
    }
}