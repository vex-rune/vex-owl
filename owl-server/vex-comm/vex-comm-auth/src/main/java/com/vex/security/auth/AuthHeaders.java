package com.vex.security.auth;

import com.vex.security.jwt.JwtClaimConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthHeaders implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    public static AuthHeaders anonymous() {
        return AuthHeaders.builder()
                .authEnabled(false)
                .userId("")
                .userName("")
                .userGroup(AuthHeaderConstants.DEFAULT_USER_GROUP)
                .loginTime(AuthHeaderConstants.DEFAULT_LOGIN_TIME)
                .role("")
                .email("")
                .nickname("")
                .sessionId("")
                .traceId("")
                .build();
    }

    public static AuthHeaders fromClaims(java.util.Map<String, Object> claims) {
        return AuthHeaders.builder()
                .authEnabled(true)
                .userId(claims.getOrDefault(JwtClaimConstants.CLAIM_USER_ID, "").toString())
                .userName(claims.getOrDefault(JwtClaimConstants.CLAIM_USER_NAME, "").toString())
                .userGroup(claims.getOrDefault(JwtClaimConstants.CLAIM_USER_GROUP, AuthHeaderConstants.DEFAULT_USER_GROUP).toString())
                .loginTime(claims.getOrDefault(JwtClaimConstants.CLAIM_LOGIN_TIME, AuthHeaderConstants.DEFAULT_LOGIN_TIME).toString())
                .role(claims.getOrDefault(JwtClaimConstants.CLAIM_ROLE, "").toString())
                .email(claims.getOrDefault(JwtClaimConstants.CLAIM_EMAIL, "").toString())
                .nickname(claims.getOrDefault(JwtClaimConstants.CLAIM_NICKNAME, "").toString())
                .build();
    }

    public java.util.Map<String, String> toHeaders() {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put(AuthHeaderConstants.HEADER_AUTH_ENABLED, authEnabled ? AuthHeaderConstants.AUTH_ENABLED_TRUE : AuthHeaderConstants.AUTH_ENABLED_FALSE);
        headers.put(AuthHeaderConstants.HEADER_USER_ID, userId != null ? userId : "");
        headers.put(AuthHeaderConstants.HEADER_USER_NAME, userName != null ? userName : "");
        headers.put(AuthHeaderConstants.HEADER_USER_GROUP, userGroup != null ? userGroup : AuthHeaderConstants.DEFAULT_USER_GROUP);
        headers.put(AuthHeaderConstants.HEADER_LOGIN_TIME, loginTime != null ? loginTime : AuthHeaderConstants.DEFAULT_LOGIN_TIME);
        headers.put(AuthHeaderConstants.HEADER_ROLE, role != null ? role : "");
        headers.put(AuthHeaderConstants.HEADER_EMAIL, email != null ? email : "");
        headers.put(AuthHeaderConstants.HEADER_NICKNAME, nickname != null ? nickname : "");
        headers.put(AuthHeaderConstants.HEADER_SESSION_ID, sessionId != null ? sessionId : "");
        headers.put(AuthHeaderConstants.HEADER_TRACE_ID, traceId != null ? traceId : "");
        return headers;
    }
}