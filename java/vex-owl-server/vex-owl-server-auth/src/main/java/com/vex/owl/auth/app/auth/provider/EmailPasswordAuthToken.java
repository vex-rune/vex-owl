//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.vex.owl.auth.app.auth.provider;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import javax.security.auth.Subject;

@Data
public class EmailPasswordAuthToken implements Authentication {
    private static final long serialVersionUID = 620L;

    private Object authorities;
    private Object credentials;
    private Object details;
    private Object principal;
    private String name;
    private boolean isAuthenticated;

    public EmailPasswordAuthToken(String email, Supplier<String> password) {
        this.name = email;
        this.principal = email;
        this.credentials = password;
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }
}
