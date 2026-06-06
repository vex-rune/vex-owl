package com.vex.owl.user.user.auth.app.auth.provider;

import com.vex.owl.user.user.auth.domain.subject.SubjectManager;
import com.vex.owl.user.user.auth.domain.subject.entity.SubjectEntity;
import com.vex.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
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
public class SubjectIdAuthProvider implements AuthenticationProvider {

    private final SubjectManager subjectManager;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String subjectId = authentication.getName();

        Optional<SubjectEntity> optSubject = subjectManager.findById(subjectId);
        SubjectEntity subject = optSubject.orElseThrow(() ->
                new IllegalArgumentException("主体不存在: " + subjectId));

        List<SimpleGrantedAuthority> grantedAuthorities = buildGrantedAuthorities(subject.getRole());
        List<String> roleAuthorities = buildRoleAuthorities(subject.getRole());

        LoginUser loginUser = LoginUser.builder()
                .subjectId(subject.getId())
                .nickName(subject.getNickname())
                .email(subject.getEmail())
                .authorities(roleAuthorities)
                .build();

        return new UsernamePasswordAuthenticationToken(
                loginUser,
                null,
                grantedAuthorities
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SubjectIdAuthToken.class.isAssignableFrom(authentication);
    }

    private List<SimpleGrantedAuthority> buildGrantedAuthorities(String role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    private List<String> buildRoleAuthorities(String role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role);
    }
}