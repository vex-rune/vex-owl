package com.vex.owl.auth.app;

import com.vex.owl.auth.domain.account.AccountBasicWithIdEntity;
import com.vex.owl.auth.domain.code.CodeManager;
import com.vex.owl.auth.domain.login_record.LoginRecordManager;
import com.vex.owl.auth.domain.subject.SubjectManager;
import com.vex.owl.auth.domain.token.TokenManager;
import com.vex.owl.auth.domain.account.AccountManager;

/**
 * 认证应用
 * 主要负责登录和注册业务流程
 */
public class AuthApp {

    private final SubjectManager subjectManager;
    private final AccountManager accountManager;
    private final LoginRecordManager loginRecordManager;
    private final TokenManager tokenManager;
    private final CodeManager codeManager;
    private final com.vex.owl.auth.domain.password.service.PasswordValidator passwordValidator;

    public AuthApp(SubjectManager subjectManager, 
                   AccountManager accountManager,
                   LoginRecordManager loginRecordManager,
                   TokenManager tokenManager,
                   CodeManager codeManager,
                   com.vex.owl.auth.domain.password.service.PasswordValidator passwordValidator) {
        this.subjectManager = subjectManager;
        this.accountManager = accountManager;
        this.loginRecordManager = loginRecordManager;
        this.tokenManager = tokenManager;
        this.codeManager = codeManager;
        this.passwordValidator = passwordValidator;
    }

    /**
     * 邮箱密码登录
     * @param email 邮箱
     * @param password 密码
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 登录结果
     */
    public LoginResult emailPasswordLogin(String email, String password, String clientIp, String deviceInfo) {
        com.vex.owl.auth.domain.login_record.entity.LoginRecord record = 
            new com.vex.owl.auth.domain.login_record.entity.LoginRecord(email, "EMAIL_PASSWORD", "PROCESSING", clientIp, deviceInfo);
        
        try {
            com.vex.owl.auth.domain.subject.entity.Subject subject = subjectManager.findByEmail(email);
            if (subject == null) {
                record.markFail("账号不存在");
                loginRecordManager.create(record);
                return null;
            }
            
            if (!subject.isActive()) {
                record.markFail("账号已被禁用");
                loginRecordManager.create(record);
                return null;
            }
            
            AccountBasicWithIdEntity account = accountManager.findBySubjectIdAndType(subject.getId(), "PASSWORD");
            if (account == null) {
                record.markFail("未设置密码");
                loginRecordManager.create(record);
                return null;
            }
            
            // TODO: 验证密码
            
            record.markSuccess(subject.getId());
            loginRecordManager.create(record);
            
            account.updateLastLogin(clientIp);
            accountManager.update(account);
            
            return generateLoginResult(subject, "EMAIL_PASSWORD");
            
        } catch (Exception e) {
            record.markFail("系统异常");
            loginRecordManager.create(record);
            return null;
        }
    }

    /**
     * 邮箱验证码登录
     * @param email 邮箱
     * @param code 验证码
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 登录结果
     */
    public LoginResult emailCodeLogin(String email, String code, String clientIp, String deviceInfo) {
        com.vex.owl.auth.domain.login_record.entity.LoginRecord record = 
            new com.vex.owl.auth.domain.login_record.entity.LoginRecord(email, "EMAIL_CODE", "PROCESSING", clientIp, deviceInfo);
        
        try {
            if (!codeManager.validateCode(email, "LOGIN", code)) {
                record.markFail("验证码错误或已过期");
                loginRecordManager.create(record);
                return null;
            }
            
            com.vex.owl.auth.domain.subject.entity.Subject subject = subjectManager.findByEmail(email);
            if (subject == null) {
                record.markFail("账号不存在");
                loginRecordManager.create(record);
                return null;
            }
            
            if (!subject.isActive()) {
                record.markFail("账号已被禁用");
                loginRecordManager.create(record);
                return null;
            }
            
            record.markSuccess(subject.getId());
            loginRecordManager.create(record);
            
            return generateLoginResult(subject, "EMAIL_CODE");
            
        } catch (Exception e) {
            record.markFail("系统异常");
            loginRecordManager.create(record);
            return null;
        }
    }

    /**
     * 注册
     * @param email 邮箱
     * @param code 验证码
     * @param password 密码（可选）
     * @param nickname 昵称
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 注册结果
     */
    public LoginResult register(String email, String code, String password, String nickname, String clientIp, String deviceInfo) {
        com.vex.owl.auth.domain.login_record.entity.LoginRecord record = 
            new com.vex.owl.auth.domain.login_record.entity.LoginRecord(email, "REGISTER", "PROCESSING", clientIp, deviceInfo);
        
        try {
            if (!codeManager.validateCode(email, "REGISTER", code)) {
                record.markFail("验证码错误或已过期");
                loginRecordManager.create(record);
                return null;
            }
            
            if (subjectManager.existsByEmail(email)) {
                record.markFail("邮箱已被注册");
                loginRecordManager.create(record);
                return null;
            }
            
            com.vex.owl.auth.domain.subject.entity.Subject subject = 
                new com.vex.owl.auth.domain.subject.entity.Subject(email, 
                    nickname != null ? nickname : email.split("@")[0], "USER");
            subjectManager.create(subject);
            
            if (password != null && !password.isEmpty()) {
                AccountBasicWithIdEntity passwordAccount =
                    new AccountBasicWithIdEntity(subject.getId(), "PASSWORD", password);
                accountManager.create(passwordAccount);
            }
            
            AccountBasicWithIdEntity emailCodeAccount =
                new AccountBasicWithIdEntity(subject.getId(), "EMAIL_CODE", null);
            accountManager.create(emailCodeAccount);
            
            record.markSuccess(subject.getId());
            loginRecordManager.create(record);
            
            return generateLoginResult(subject, "REGISTER");
            
        } catch (Exception e) {
            record.markFail("系统异常");
            loginRecordManager.create(record);
            return null;
        }
    }

    /**
     * 管理员登录
     * @param email 邮箱
     * @param password 密码
     * @param clientIp 客户端IP
     * @param deviceInfo 设备信息
     * @return 登录结果
     */
    public LoginResult adminLogin(String email, String password, String clientIp, String deviceInfo) {
        com.vex.owl.auth.domain.login_record.entity.LoginRecord record = 
            new com.vex.owl.auth.domain.login_record.entity.LoginRecord(email, "ADMIN_LOGIN", "PROCESSING", clientIp, deviceInfo);
        
        try {
            com.vex.owl.auth.domain.subject.entity.Subject subject = subjectManager.findByEmail(email);
            if (subject == null) {
                record.markFail("账号不存在");
                loginRecordManager.create(record);
                return null;
            }
            
            if (!subject.isAdmin()) {
                record.markFail("非管理员账号");
                loginRecordManager.create(record);
                return null;
            }
            
            AccountBasicWithIdEntity account = accountManager.findBySubjectIdAndType(subject.getId(), "PASSWORD");
            if (account == null) {
                record.markFail("未设置密码");
                loginRecordManager.create(record);
                return null;
            }
            
            // TODO: 验证密码
            
            record.markSuccess(subject.getId());
            loginRecordManager.create(record);
            
            account.updateLastLogin(clientIp);
            accountManager.update(account);
            
            return generateLoginResult(subject, "ADMIN_LOGIN");
            
        } catch (Exception e) {
            record.markFail("系统异常");
            loginRecordManager.create(record);
            return null;
        }
    }

    /**
     * 退出登录
     * @param token Token
     */
    public void logout(String token) {
        if (token != null && !token.isEmpty()) {
            tokenManager.blacklistToken(token);
        }
    }

    private LoginResult generateLoginResult(com.vex.owl.auth.domain.subject.entity.Subject subject, String loginType) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("email", subject.getEmail());
        claims.put("role", subject.getRole());
        claims.put("type", loginType);
        
        String accessToken = tokenManager.generateAccessToken(subject.getId().toString(), claims);
        String refreshToken = tokenManager.generateRefreshToken(subject.getId().toString());
        
        return new LoginResult(accessToken, refreshToken, 3600L, subject.getEmail(), subject.getNickname(), subject.getRole());
    }

    /**
     * 登录结果
     */
    public static class LoginResult {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private String email;
        private String nickname;
        private String role;

        public LoginResult(String accessToken, String refreshToken, Long expiresIn, String email, String nickname, String role) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
            this.email = email;
            this.nickname = nickname;
            this.role = role;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }

        public String getEmail() {
            return email;
        }

        public String getNickname() {
            return nickname;
        }

        public String getRole() {
            return role;
        }
    }
}