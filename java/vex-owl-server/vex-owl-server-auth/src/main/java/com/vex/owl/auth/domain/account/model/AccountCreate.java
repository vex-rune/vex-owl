package com.vex.owl.auth.domain.account.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

import java.util.function.Supplier;

/**
 * 账号创建请求记录
 *
 * @param subjectId   主体ID
 * @param accountType 账号类型
 * @param account     账号
 * @param password    账号凭证（密码处理函数）
 */
public record AccountCreate(
        @NotNull @Max(50) String subjectId,
        @NotNull @Max(50) AccountType accountType,
        @NotNull @Max(150) String account,
        @NotNull @Max(50) Supplier<String> password
) {
}
