package com.vex.owl.auth.api;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.api.request.LoginRequest;
import com.vex.owl.auth.api.request.RegisterRequest;
import com.vex.owl.auth.api.request.SendCodeRequest;
import com.vex.owl.auth.app.auth.AuthApp;
import com.vex.owl.auth.app.auth.LoginType;
import com.vex.security.jwt.VexToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块
 * <p>认证相关业务接口</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthApp authApp;

    /**
     * 认证-登录
     * <p>用户登录，支持三种登录方式：管理员登录、邮箱+密码登录、邮箱+验证码登录</p>
     * <pre>
     * 请求示例:
     * 1. 管理员: {"principal": "admin", "credentials": "123456", "loginType": "admin"}
     * 2. 邮箱+密码: {"principal": "user@example.com", "credentials": "password123", "loginType": "email_password"}
     * 3. 邮箱+验证码: {"principal": "user@example.com", "credentials": "123456", "loginType": "email_code"}
     * </pre>
     * <p>注意：内部登录（loginType为空或null）不允许通过此接口调用</p>
     */
    @PostMapping("/login")
    public ApiResponse<VexToken> login(@Valid @RequestBody LoginRequest request) {
        if (request.getLoginType() == null || request.getLoginType().isBlank()) {
            throw new IllegalArgumentException("不允许使用内部登录方式，请指定有效的 loginType");
        }
        LoginType loginType = LoginType.fromValue(request.getLoginType());
        VexToken token = authApp.login(request.getPrincipal(), request.getCredentials(), loginType);
        return ApiResponse.success(token);
    }

    /**
     * 认证-发送验证码
     * <p>发送注册验证码到邮箱</p>
     *
     * @return
     */
    @PostMapping("/send/register/code")
    public ApiResponse<Object> sendRegisterCode(@Valid @RequestBody SendCodeRequest request) {
        authApp.sendRegisterCode(request.getEmail());
        return ApiResponse.success();
    }

    /**
     * 认证-发送登录验证码
     * <p>发送登录验证码到邮箱</p>
     *
     * @return
     */
    @PostMapping("/send/login/code")
    public ApiResponse<Object> sendLoginCode(@Valid @RequestBody SendCodeRequest request) {
        authApp.sendLoginCode(request.getEmail());
        return ApiResponse.success();
    }

    /**
     * 认证-注册
     * <p>用户注册，注册成功后自动登录并返回token</p>
     */
    @PostMapping("/register")
    public ApiResponse<VexToken> register(@Valid @RequestBody RegisterRequest request) {
        VexToken token = authApp.register(request.getEmail(), request.getCode(), request.getPassword(), request.getNickname());
        return ApiResponse.success(token);
    }
}