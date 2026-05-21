package com.vex.owl.auth.app.auth;

import com.vex.owl.auth.app.auth.provider.AdminAuthToken;
import com.vex.owl.auth.app.auth.provider.EmailCodeAuthToken;
import com.vex.owl.auth.app.auth.provider.EmailPasswordAuthToken;
import com.vex.owl.auth.app.auth.provider.SubjectIdAuthToken;
import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.owl.auth.domain.account.model.*;
import com.vex.owl.auth.domain.code.model.CodeEntity;
import com.vex.owl.auth.domain.code.model.CodeId;
import com.vex.owl.auth.domain.code.repo.CodeRedisRepository;
import com.vex.owl.auth.domain.subject.SubjectManager;
import com.vex.owl.auth.domain.subject.entity.SubjectEntity;
import com.vex.owl.notification.api.client.NotificationClient;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import com.vex.security.LoginUser;
import com.vex.security.jwt.JwtTokenProvider;
import com.vex.security.jwt.VexToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 认证应用服务
 * 处理注册和登录业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApp {

    private static final String CODE_TYPE_REGISTER = "email_register";
    private static final String CODE_TYPE_LOGIN = "email_login";
    private static final String MAIL_TEMPLATE_REGISTER_CODE = "VEX_MAIL_REGISTER_CODE";
    private static final String MAIL_TEMPLATE_LOGIN_CODE = "VEX_MAIL_LOGIN_CODE";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SubjectManager subjectManager;
    private final AccountManager accountManager;
    private final CodeRedisRepository codeRepository;
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
     * @param principal 身份标识
     * @param credentials 凭证/密钥
     * @param loginType 登录方式
     * @return 用户会话凭证
     */
    public VexToken login(String principal, String credentials, LoginType loginType) {
        log.info("[登录] principal: {}, loginType: {}", principal, loginType.getValue());

        Authentication authentication;
        switch (loginType) {
            case ADMIN:
                authentication = authenticationManager.authenticate(new AdminAuthToken(principal, () -> credentials));
                break;
            case EMAIL_PASSWORD:
                authentication = authenticationManager.authenticate(new EmailPasswordAuthToken(principal, () -> credentials));
                break;
            case EMAIL_CODE:
                authentication = authenticationManager.authenticate(new EmailCodeAuthToken(principal, () -> credentials));
                break;
            case INTERNAL:
                authentication = authenticationManager.authenticate(new SubjectIdAuthToken(principal));
                break;
            default:
                throw new IllegalArgumentException("不支持的登录方式: " + loginType);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginUser user = (LoginUser) authentication.getPrincipal();
        log.info("[登录] 成功, principal: {}, loginType: {}", principal, loginType.getValue());
        return jwtTokenProvider.generateByUser(user);
    }

    /**
     * 注册
     *
     * @param email 邮箱
     * @param code 验证码
     * @param password 密码
     * @param nickname 昵称
     * @return 用户会话凭证
     */
    public VexToken register(String email, String code, String password, String nickname) {
        CodeEntity codeEntity = codeRepository.findByIdAndCode(
                        new CodeId(email, CODE_TYPE_REGISTER),
                        code)
                .orElseThrow(() -> new IllegalArgumentException("验证码错误或已过期"));
        codeRepository.delete(codeEntity);

        String subjectId = UUID.randomUUID().toString();
        SubjectEntity subject = SubjectEntity.builder()
                .id(subjectId)
                .email(email)
                .nickname(nickname)
                .build();
        subjectManager.create(subject);

        accountManager.create(new AccountCreate(
                subjectId,
                AccountType.email,
                email,
                () -> password
        ));

        log.info("[注册] 成功, email: {}, subjectId: {}", email, subjectId);
        return login(subjectId, null, LoginType.INTERNAL);
    }

    /**
     * 发送注册验证码
     *
     * @param email 邮箱地址
     */
    public void sendRegisterCode(String email) {
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        CodeEntity codeEntity = CodeEntity.builder()
                .id(new CodeId(email, CODE_TYPE_REGISTER))
                .code(code)
                .build();
        codeRepository.save(codeEntity);

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
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        CodeEntity codeEntity = CodeEntity.builder()
                .id(new CodeId(email, CODE_TYPE_LOGIN))
                .code(code)
                .build();
        codeRepository.save(codeEntity);

        SendEmailRequest sendEmailRequest = new SendEmailRequest(
                email,
                MAIL_TEMPLATE_LOGIN_CODE,
                Map.of("code", code)
        );
        notificationClient.sendEmail(sendEmailRequest);
    }

}