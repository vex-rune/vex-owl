package com.vex.owl.auth.domain.code;

/**
 * 验证码管理
 * 负责验证码的生成、存储、验证
 */
public class CodeManager {

    /**
     * 生成验证码
     * @param email 邮箱
     * @param type 类型（LOGIN/REGISTER）
     * @return 验证码
     */
    public String generateCode(String email, String type) {
        return null;
    }

    /**
     * 验证验证码
     * @param email 邮箱
     * @param type 类型
     * @param code 验证码
     * @return 是否有效
     */
    public boolean validateCode(String email, String type, String code) {
        return false;
    }

    /**
     * 发送验证码
     * @param email 邮箱
     * @param type 类型
     */
    public void sendCode(String email, String type) {
    }
}