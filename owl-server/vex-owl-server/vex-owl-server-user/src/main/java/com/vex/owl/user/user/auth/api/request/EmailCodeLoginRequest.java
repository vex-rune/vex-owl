package com.vex.owl.user.user.auth.api.request;

/**
 * 验证码登录请求
 */
public class EmailCodeLoginRequest {

    private String email;
    private String code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}