package com.vex.owl.auth.api;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.api.request.LoginRequest;
import com.vex.owl.auth.api.request.RegisterRequest;
import com.vex.owl.auth.api.response.LoginResponse;
import com.vex.owl.auth.app.AuthApp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供注册和登录接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final AuthApp authApp;

}
