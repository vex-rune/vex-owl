//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.vex.owl.user.user.auth.app.auth.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.security.auth.Subject;

@Data
public class AdminAuthToken implements Authentication {
    private static final long serialVersionUID = 620L;

    private Collection<? extends GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ADMIN"));
    private Object credentials;
    private Object details;
    private Object principal;
    private String name;
    private boolean isAuthenticated;

    public AdminAuthToken(String email, Supplier<String> password) {
        this.name = email;
        this.principal = email;
        this.credentials = password;
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }


}
