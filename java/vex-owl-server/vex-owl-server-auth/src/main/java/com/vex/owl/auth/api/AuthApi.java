package com.vex.owl.auth.api;

import com.vex.owl.auth.api.request.*;
import com.vex.owl.auth.api.response.*;
import com.vex.owl.auth.app.AuthApp;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证接口
 * 提供登录、注册、登出等功能
 */
public class AuthApi {

    private final AuthApp authApp;

    public AuthApi(AuthApp authApp) {
        this.authApp = authApp;
    }

    /**
     * 邮箱密码登录
     */
    public ApiResponse<LoginResponse> loginByEmail(LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String deviceInfo = httpRequest.getHeader("User-Agent");
        
        AuthApp.LoginResult result = authApp.emailPasswordLogin(
            request.getEmail(), 
            request.getPassword(), 
            clientIp, 
            deviceInfo
        );
        
        if (result == null) {
            return ApiResponse.error("登录失败");
        }
        
        LoginResponse response = new LoginResponse(
            result.getAccessToken(),
            result.getRefreshToken(),
            result.getExpiresIn(),
            result.getEmail(),
            result.getNickname(),
            result.getRole()
        );
        
        return ApiResponse.success(response);
    }

    /**
     * 邮箱验证码登录
     */
    public ApiResponse<LoginResponse> loginByCode(EmailCodeLoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String deviceInfo = httpRequest.getHeader("User-Agent");
        
        AuthApp.LoginResult result = authApp.emailCodeLogin(
            request.getEmail(),
            request.getCode(),
            clientIp,
            deviceInfo
        );
        
        if (result == null) {
            return ApiResponse.error("登录失败");
        }
        
        LoginResponse response = new LoginResponse(
            result.getAccessToken(),
            result.getRefreshToken(),
            result.getExpiresIn(),
            result.getEmail(),
            result.getNickname(),
            result.getRole()
        );
        
        return ApiResponse.success(response);
    }

    /**
     * 注册
     */
    public ApiResponse<LoginResponse> register(RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        String deviceInfo = httpRequest.getHeader("User-Agent");
        
        AuthApp.LoginResult result = authApp.register(
            request.getEmail(),
            request.getCode(),
            request.getPassword(),
            request.getNickname(),
            clientIp,
            deviceInfo
        );
        
        if (result == null) {
            return ApiResponse.error("注册失败");
        }
        
        LoginResponse response = new LoginResponse(
            result.getAccessToken(),
            result.getRefreshToken(),
            result.getExpiresIn(),
            result.getEmail(),
            result.getNickname(),
            result.getRole()
        );
        
        return ApiResponse.success(response);
    }

    /**
     * 发送验证码
     */
    public ApiResponse<Void> sendCode(SendCodeRequest request) {
        return ApiResponse.success("验证码已发送", null);
    }

    /**
     * 退出登录
     */
    public ApiResponse<Void> logout(String token) {
        authApp.logout(token);
        return ApiResponse.success("退出成功", null);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}