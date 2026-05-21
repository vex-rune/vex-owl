package com.vex.owl.auth.domain.account.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.util.function.Supplier;

/**
 * 账号更新请求记录
 *
 * @param id        账号ID
 * @param account   账号（可选）
 * @param password  新密码（可选，如果提供则重新加密）
 */
public record AccountUpdate(
        @NotNull AccountId id,
        @Max(150) String account,
        Supplier<String> password
) {
}
