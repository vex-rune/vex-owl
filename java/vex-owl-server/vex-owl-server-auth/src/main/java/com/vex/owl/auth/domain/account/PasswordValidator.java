package com.vex.owl.auth.domain.account;

/**
 * 密码校验器
 * 核心领域服务，纯Java实现密码加密和验证逻辑
 */
public class PasswordValidator {

    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @param encoder 密码编码器实现
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @param encoder 密码编码器实现
     * @return 加密后的密码
     */
    public String encode(String rawPassword, PasswordEncoder encoder) {
        return encoder.encode(rawPassword);
    }

    /**
     * 密码格式校验
     * @param password 密码
     * @return 是否有效
     */
    public boolean isValidFormat(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 6 && password.length() <= 50;
    }

    /**
     * 密码编码器接口
     */
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }
}