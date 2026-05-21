package com.vex.owl.auth.domain.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountId {

    /// 主体ID
    @Column(nullable = false)
    @NotNull
    @Max(50)
    private String subjectId;

    /// 账号类型
    @Column(nullable = false, length = 50)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Max(50)
    private AccountType accountType;
}
