package com.vex.owl.auth.app.auth;

/**
 * 登录方式枚举
 * <p>定义系统支持的登录方式</p>
 * <pre>
 * 使用说明:
 * - ADMIN: 管理员登录，使用固定凭证(credentials="123456")
 * - EMAIL_PASSWORD: 邮箱+密码登录，principal为邮箱地址，credentials为密码
 * - EMAIL_CODE: 邮箱+验证码登录，principal为邮箱地址，credentials为6位数字验证码
 * - INTERNAL: 内部登录，principal为subjectId，仅供内部服务调用
 * </pre>
 */
public enum LoginType {

    ADMIN("admin", "管理员登录"),
    EMAIL_PASSWORD("email_password", "邮箱+密码登录"),
    EMAIL_CODE("email_code", "邮箱+验证码登录"),
    INTERNAL("internal", "内部登录（仅供内部服务调用）");

    private final String value;
    private final String description;

    LoginType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static LoginType fromValue(String value) {
        for (LoginType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的登录方式: " + value);
    }
}