package com.vex.owl.user.user.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求
 * <p>principal: 身份标识，根据登录类型不同可以是邮箱、管理员账号等</p>
 * <p>credentials: 凭证/密钥，根据登录类型不同可以是密码、验证码等</p>
 * <p>loginType: 登录方式枚举</p>
 * <pre>
 * 使用示例:
 * 1. 管理员登录: principal="admin", credentials="123456", loginType="admin"
 * 2. 邮箱+密码: principal="user@example.com", credentials="password123", loginType="email_password"
 * 3. 邮箱+验证码: principal="user@example.com", credentials="123456", loginType="email_code"
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "身份标识不能为空")
    private String principal;

    private String credentials;

    @NotBlank(message = "登录方式不能为空")
    private String loginType;
}