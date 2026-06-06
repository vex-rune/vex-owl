package com.vex.security.jwt;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * VEX 系统 Token 响应对象
 * 包含访问令牌、刷新令牌及用户基本信息
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VexToken {
    /**
     * 访问令牌 (Access Token)
     * 用于身份验证和资源访问
     */
    private String accessToken;

    /**
     * 刷新令牌 (Refresh Token)
     * 用于在访问令牌过期后获取新的访问令牌
     */
    private String refreshToken;

    /**
     * 令牌有效期（秒）
     */
    private Long expiresIn;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户角色
     */
    private String role;
}
