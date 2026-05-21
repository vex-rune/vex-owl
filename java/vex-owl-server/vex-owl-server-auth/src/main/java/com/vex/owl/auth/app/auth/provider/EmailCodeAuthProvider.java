package com.vex.owl.auth.app.auth.provider;

import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.owl.auth.domain.account.model.AccountEntity;
import com.vex.owl.auth.domain.account.model.AccountType;
import com.vex.owl.auth.domain.code.model.CodeEntity;
import com.vex.owl.auth.domain.code.model.CodeId;
import com.vex.owl.auth.domain.code.repo.CodeRedisRepository;
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
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailCodeAuthProvider implements AuthenticationProvider {


    public static final SimpleGrantedAuthority USER_GRANTED_AUTHORITY = new SimpleGrantedAuthority("USER");

    private final AccountManager accountManager;
    private final SubjectManager subjectManager;
    private final CodeRedisRepository codeRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String code = authentication.getCredentials().toString();

        CodeEntity codeEntity = codeRepository.findByIdAndCode(new CodeId(email, "email_login"), code)
                .orElseThrow(() -> new BadCredentialsException("验证码错误或已过期"));

        Optional<AccountEntity> optAccount = accountManager.validByAccount(AccountType.email, email);
        AccountEntity account = optAccount.orElseThrow(() -> new AccountExpiredException("账号不存在"));

        codeRepository.delete(codeEntity);

        List<SimpleGrantedAuthority> auths = Collections.singletonList(USER_GRANTED_AUTHORITY);

        Optional<SubjectEntity> optSub = subjectManager.findById(account.getSubjectId());
        SubjectEntity subject = optSub.orElseThrow(() -> new AccountExpiredException("主体不存在"));

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

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailCodeAuthToken.class.isAssignableFrom(authentication);
    }
}