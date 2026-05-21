package com.vex.security.jwt;

import lombok.Getter;

@Getter
public class Tokenor {
    /// 当前用户的token
    private String token;
    /// 刷新 token
    private String refreshToken;
}
