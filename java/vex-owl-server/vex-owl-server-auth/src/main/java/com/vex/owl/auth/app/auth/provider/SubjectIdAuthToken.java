package com.vex.owl.auth.app.auth.provider;

import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

@Data
public class SubjectIdAuthToken implements Authentication {
    private static final long serialVersionUID = 623L;

    private Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
    private Object credentials;
    private Object details;
    private Object principal;
    private String name;
    private boolean isAuthenticated;

    public SubjectIdAuthToken(String subjectId) {
        this.name = subjectId;
        this.principal = subjectId;
        this.credentials = null;
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }
}