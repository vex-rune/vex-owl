package com.vex.owl.user.user.auth.app.auth;

import com.vex.model.VexException;
import com.vex.owl.notification.api.client.NotificationClient;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import com.vex.owl.user.user.auth.app.auth.provider.AdminAuthToken;
import com.vex.owl.user.user.auth.app.auth.provider.EmailCodeAuthToken;
import com.vex.owl.user.user.auth.app.auth.provider.EmailPasswordAuthToken;
import com.vex.owl.user.user.auth.app.auth.provider.SubjectIdAuthToken;
import com.vex.owl.user.user.auth.domain.account.AccountManager;
import com.vex.owl.user.user.auth.domain.account.model.AccountCreate;
import com.vex.owl.user.user.auth.domain.account.model.AccountType;
import com.vex.owl.user.user.auth.domain.code.CodeManager;
import com.vex.owl.user.user.auth.domain.subject.SubjectManager;
import com.vex.owl.user.user.auth.domain.subject.entity.SubjectEntity;
import com.vex.security.auth.AuthUser;
import com.vex.security.jwt.JwtTokenProvider;
import com.vex.security.jwt.VexToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 认证应用服务
 * 处理注册和登录业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApp {

    private static final String CODE_TYPE_REGISTER = "email_register";
    public static final String CODE_TYPE_LOGIN = "email_login";
    private static final String MAIL_TEMPLATE_REGISTER_CODE = "VEX_MAIL_REGISTER_CODE";
    private static final String MAIL_TEMPLATE_LOGIN_CODE = "VEX_MAIL_LOGIN_CODE";

    private final ProviderManager providerManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SubjectManager subjectManager;
    private final AccountManager accountManager;
    private final CodeManager codeManager;
    private final NotificationClient notificationClient;

    /**
     * 登录
     * <p>支持多种登录方式，根据 loginType 分发到不同的认证器</p>
     * <pre>
     * 使用说明:
     * - ADMIN: principal=管理员账号, credentials=固定凭证
     * - EMAIL_PASSWORD: principal=邮箱地址, credentials=密码
     * - EMAIL_CODE: principal=邮箱地址, credentials=6位验证码
     * - INTERNAL: principal=subjectId，仅供内部服务调用
     * </pre>
     *
     * @param principal   身份标识
     * @param credentials 凭证/密钥
     * @param loginType   登录方式
     * @return 用户会话凭证
     */
    public VexToken login(String principal, String credentials, LoginType loginType) {
        log.debug("[登录] principal: {}, loginType: {}", principal, loginType.getValue());

        Authentication token = switch (loginType) {
            case ADMIN -> new AdminAuthToken(principal, credentials);
            case EMAIL_PASSWORD -> new EmailPasswordAuthToken(principal, credentials);
            case EMAIL_CODE -> new EmailCodeAuthToken(principal,  credentials);
            case INTERNAL -> new SubjectIdAuthToken(principal);
        };


        Authentication authentication = providerManager.authenticate(token);
        AuthUser user = (AuthUser) authentication.getPrincipal();
        log.debug("[登录] 成功, principal: {}, loginType: {}", principal, loginType.getValue());
        return jwtTokenProvider.generateByUser(user);
    }

    /**
     * 注册
     *
     * @param email    邮箱
     * @param code     验证码
     * @param password 密码
     * @param nickname 昵称
     * @return 用户会话凭证
     */
    public VexToken register(String email, String code, String password, String nickname) {
        boolean b = codeManager.validateCode(email, CODE_TYPE_REGISTER, code);

        if (!b) {
            throw new VexException("CODE_INVALID", "验证码错误或已过期");
        }

        codeManager.deleteCode(email, CODE_TYPE_REGISTER);

        SubjectEntity subject = SubjectEntity.builder()
                .email(email)
                .nickname(nickname)
                .role("USER")
                .build();
        subjectManager.create(subject);

        accountManager.create(new AccountCreate(
                subject.getId(),
                AccountType.email,
                email,
                () -> password
        ));

        log.debug("[注册] 成功, email: {}, subjectId: {}", email, subject.getId());
        return login(subject.getId(), null, LoginType.INTERNAL);
    }

    /**
     * 发送注册验证码
     *
     * @param email 邮箱地址
     */
    public void sendRegisterCode(String email) {

        String code = codeManager.generateCode(email, CODE_TYPE_REGISTER);

        SendEmailRequest sendEmailRequest = new SendEmailRequest(
                email,
                MAIL_TEMPLATE_REGISTER_CODE,
                Map.of("code", code)
        );
        notificationClient.sendEmail(sendEmailRequest);
    }

    /**
     * 发送登录验证码
     *
     * @param email 邮箱地址
     */
    public void sendLoginCode(String email) {

        String code = codeManager.generateCode(email, CODE_TYPE_LOGIN);

        SendEmailRequest sendEmailRequest = new SendEmailRequest(
                email,
                MAIL_TEMPLATE_LOGIN_CODE,
                Map.of("code", code)
        );
        notificationClient.sendEmail(sendEmailRequest);
    }

}