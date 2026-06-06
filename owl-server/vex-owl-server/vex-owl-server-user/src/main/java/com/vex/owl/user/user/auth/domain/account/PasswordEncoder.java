package com.vex.owl.user.user.auth.domain.account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具类
 * 提供密码加盐哈希功能
 */
public class PasswordEncoder {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16; // 128位盐值

    /**
     * 生成随机盐值
     *
     * @return Base64编码的盐值字符串
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * 对密码进行加盐哈希加密
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 加密后的密码（Base64编码）
     */
    public static String encrypt(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param salt            盐值
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String salt, String encryptedPassword) {
        String encryptedInput = encrypt(rawPassword, salt);
        return encryptedInput.equals(encryptedPassword);
    }
}
