package com.vex.owl.user.user.auth.domain.login_record.entity;

import com.vex.queries.jpa.id.BizIdPrefix;
import com.vex.queries.jpa.id.BizSnowId;
import com.vex.queries.jpa.model.JpaBasicWithIdEntity;
import jakarta.persistence.*;

/**
 * 登录日志
 * 记录用户登录行为
 */
@Entity
@Table(name = "auth_login_record")
@BizIdPrefix(value = "LOG")
public class LoginRecordEntity extends JpaBasicWithIdEntity {

    @Id
    @BizSnowId
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