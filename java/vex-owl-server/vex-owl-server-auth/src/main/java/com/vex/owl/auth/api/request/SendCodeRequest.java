package com.vex.owl.auth.api.request;

/**
 * 发送验证码请求
 */
public class SendCodeRequest {

    private String email;
    private String type;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}