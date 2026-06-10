package com.vex.security.web;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户上下文（AuthUserFilter 写入）
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