package com.vex.security.web.filter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

@AutoConfiguration
@ConditionalOnClass(WebFilter.class)
@ConditionalOnProperty(prefix = "vex.auth.user-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthUserFilterAutoConfiguration {

    @Bean
    public AuthUserFilter authUserFilter() {
        return new AuthUserFilter();
    }

    @Bean
    public CurrentUserResolverImpl currentUserResolver() {
        return new CurrentUserResolverImpl();
    }
}
