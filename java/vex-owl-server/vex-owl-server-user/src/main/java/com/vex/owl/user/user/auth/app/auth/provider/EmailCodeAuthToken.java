package com.vex.owl.user.user.auth.app.auth.provider;

import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

@Data
public class EmailCodeAuthToken implements Authentication {
    private static final long serialVersionUID = 621L;

    private Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("USER"));
    private Object credentials;
    private Object details;
    private Object principal;
    private String name;
    private boolean isAuthenticated;

    public EmailCodeAuthToken(String email, Supplier<String> code) {
        this.name = email;
        this.principal = email;
        this.credentials = code;
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }
}