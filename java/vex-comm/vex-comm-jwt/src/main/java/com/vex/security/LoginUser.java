package com.vex.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户视图对象
 * 实现 UserDetails 无缝适配 SpringSecurity
 */
@Data
public class LoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 加密密码（仅认证使用，返回前端清空）
     */
    private String password;

    /**
     * 用户状态 1正常 0禁用
     */
    private Integer status;

    /**
     * 权限/角色集合
     */
    private Collection<String> authorities;
}