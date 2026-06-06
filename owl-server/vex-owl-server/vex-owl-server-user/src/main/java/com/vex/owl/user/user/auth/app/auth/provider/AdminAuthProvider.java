package com.vex.owl.user.user.auth.app.auth.provider;

import com.vex.owl.user.user.auth.domain.account.AccountManager;
import com.vex.owl.user.user.auth.domain.subject.SubjectManager;
import com.vex.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminAuthProvider implements AuthenticationProvider {


    public static final SimpleGrantedAuthority USER_GRANTED_AUTHORITY = new SimpleGrantedAuthority("ADMIN");

    private AccountManager accountManager;
    private SubjectManager subjectManager;
    private PasswordEncoder passwordEncoder;

    /**
     * 核心认证逻辑
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 1. 获取前端传入账号密码
        String password = authentication.getCredentials().toString();


        // 2. 密码比对, 密码必须是 123456
        if (!password.equals("123456")) {
            throw new BadCredentialsException("密码错误");
        }


        // 3. 封装权限
        List<SimpleGrantedAuthority> auths = Collections.singletonList(USER_GRANTED_AUTHORITY);

        LoginUser user = LoginUser.builder()
                .subjectId("admin")
                .nickName("admin")
                .authorities(Collections.singletonList("admin"))
                .build();

        // 4. 返回已认证对象
        return new UsernamePasswordAuthenticationToken(user, null, auths);
    }

    /**
     * 适配账号密码登录令牌
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return AdminAuthToken.class.isAssignableFrom(authentication);
    }
}