package com.vex.owl.auth.app.auth.provider;

import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.owl.auth.domain.account.model.AccountEntity;
import com.vex.owl.auth.domain.account.model.AccountType;
import com.vex.owl.auth.domain.subject.SubjectManager;
import com.vex.owl.auth.domain.subject.entity.SubjectEntity;
import com.vex.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.security.auth.Subject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailPasswordAuthProvider implements AuthenticationProvider {


    public static final SimpleGrantedAuthority USER_GRANTED_AUTHORITY = new SimpleGrantedAuthority("USER");

    private final AccountManager accountManager;
    private final SubjectManager subjectManager;

    /**
     * 核心认证逻辑
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. 获取前端传入账号密码
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        // 2. 查询数据库用户
        Optional<AccountEntity> opt = accountManager.validByAccount(AccountType.email, email);

        AccountEntity account = opt.orElseThrow(() -> new AccountExpiredException("UsernameNotFound"));

        // 3. 密码比对
        if (!accountManager.checkPassword(opt.get().getId(), password)) {
            throw new BadCredentialsException("密码错误");
        }

        // 4. 封装权限
        List<SimpleGrantedAuthority> auths = Collections.singletonList(USER_GRANTED_AUTHORITY);


        // 5. 找到主体
        Optional<SubjectEntity> optSub = subjectManager.findById(account.getSubjectId());

        SubjectEntity subject = optSub.orElseThrow(() -> new AccountExpiredException("主体不存在"));

        LoginUser.builder()
                .subjectId(subject.getId())
                .nickName(subject.getNickname())
                .phone(null)
                .email(account.getCredential())
                .authorities(Collections.singletonList(subject.getRole()))
                .build();

        // 5. 返回已认证对象
        return new UsernamePasswordAuthenticationToken(LoginUser.builder()
                .subjectId(subject.getId())
                .nickName(subject.getNickname())
                .phone(null)
                .email(account.getCredential())
                .authorities(Collections.singletonList(subject.getRole()))
                .build(),
                null,
                auths
        );
    }

    /**
     * 适配账号密码登录令牌
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return EmailPasswordAuthToken.class.isAssignableFrom(authentication);
    }
}