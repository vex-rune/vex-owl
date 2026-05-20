package com.vex.owl.auth.domain.login_record.entity;

import jakarta.persistence.*;

/**
 * 登录日志
 * 记录用户登录行为
 */
@Entity
@Table(name = "auth_login_record")
public class LoginRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String loginType;

    @Column(nullable = false, length = 50)
    private String loginStatus;

    @Column(length = 500)
    private String clientIp;

    @Column(length = 500)
    private String deviceInfo;

    @Column(length = 1000)
    private String failReason;

    @Column(nullable = false, updatable = false)
    private String loginTime;

    private Long subjectId;

    public LoginRecord() {
    }

    public LoginRecord(String email, String loginType, String loginStatus, String clientIp, String deviceInfo) {
        this.email = email;
        this.loginType = loginType;
        this.loginStatus = loginStatus;
        this.clientIp = clientIp;
        this.deviceInfo = deviceInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public void markSuccess(Long subjectId) {
        this.loginStatus = "SUCCESS";
        this.subjectId = subjectId;
    }

    public void markFail(String reason) {
        this.loginStatus = "FAIL";
        this.failReason = reason;
    }

    public boolean isSuccess() {
        return "SUCCESS".equals(loginStatus);
    }
}