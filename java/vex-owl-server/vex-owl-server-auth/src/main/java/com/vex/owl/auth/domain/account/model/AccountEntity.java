package com.vex.owl.auth.domain.account.model;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 账号信息
 * 管理用户登录凭证
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@BizIdPrefix(value = "account")
@Table(name = "auth_account")
public class AccountEntity extends JpaBasicWithIdEntity {

    /// ID
    @Id
    @BizSnowId
    private String id;

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

    /// 账号
    @Column(nullable = false)
    @NotNull
    @Max(150)
    private String account;

    /// 纯小写账号
    @Column(nullable = false)
    @NotNull
    @Max(150)
    private String accountLower;

    /// 账号凭证
    @Column(nullable = false, length = 50)
    @NotNull
    @Max(50)
    private String credential;

    /// 盐
    @Column(nullable = true, length = 50)
    @NotNull
    @Max(50)
    private String salt;
}