package com.vex.owl.user.user.auth.domain.code;

import com.vex.owl.user.user.auth.domain.code.model.CodeEntity;
import com.vex.owl.user.user.auth.domain.code.repo.CodeRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * 验证码管理
 * 负责验证码的生成、存储、验证
 */
@Component
@RequiredArgsConstructor
public class CodeManager {

    public final CodeRedisRepository codeRedisRepository;

    /**
     * 生成验证码
     *
     * @param email 邮箱
     * @param type  类型（LOGIN/REGISTER）
     * @return 验证码
     */
    public String generateCode(String email, String type) {
        // 生成6位随机数字验证码
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((int) (Math.random() * 10));
        }
        // 保存验证码
        codeRedisRepository.save(
                new CodeEntity(getId(email, type), email, type, code.toString(), 300L)
        );
        return code.toString();
    }

    private static String getId(String email, String type) {
        return type + ":" + email;
    }

    /**
     * 验证验证码
     *
     * @param email 邮箱
     * @param type  类型
     * @param code  验证码
     * @return 是否有效
     */
    public boolean validateCode(String email, String type, String code) {
        String id = getId(email, type);
        Optional<CodeEntity> byId = codeRedisRepository.findById(id);
        return byId
                .filter( c-> Objects.equals(code, c.getCode()))
                .isPresent();
    }

    /**
     * 删除验证码
     */
    public void deleteCode(String email, String type) {
        codeRedisRepository.deleteById(getId(email, type));
    }

}