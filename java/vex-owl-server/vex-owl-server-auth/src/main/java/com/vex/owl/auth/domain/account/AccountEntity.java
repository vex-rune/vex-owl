package com.vex.owl.auth.domain.account;

import com.vex.query.criteria.jpa.BasicWithIdJpaEntity;
import jakarta.persistence.*;
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
@Table(name = "auth_account")
public class AccountEntity extends BasicWithIdJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(nullable = false, length = 50)
    private String accountType;

    @Column(length = 255)
    private String credential;

    @Column(length = 500)
    private String lastLoginIp;

    private String lastLoginTime;

}