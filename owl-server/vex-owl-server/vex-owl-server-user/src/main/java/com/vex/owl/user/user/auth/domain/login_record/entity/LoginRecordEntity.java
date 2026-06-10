package com.vex.owl.user.user.auth.domain.login_record.entity;

import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 登录日志
 * 记录用户登录行为
 */
@Entity
@Table(name = "user_auth_login_record")
public class LoginRecordEntity extends JpaBasicEntity {

    @Id
    @BizSnowId("log")
    private String id;

    /// 主体
    @Column
    private String subjectId;

    /// 账号ID
    @Column
    private String accountId;

    /// 登录时间
    @Column
    private Long loginTime;

    /// 登录方式
    @Column
    private String loginType;

}