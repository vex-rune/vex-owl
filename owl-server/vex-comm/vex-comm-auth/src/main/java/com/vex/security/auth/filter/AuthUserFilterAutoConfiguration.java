package com.vex.security.auth.filter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

/**
 * AuthUserFilter 自动配置
 *
 * <p>当 classpath 存在 WebFlux（{@link WebFilter}）时自动注册 {@link AuthUserFilter}。</p>
 * <p>可通过 {@code vex.auth.user-filter.enabled=false} 关闭。</p>
 */
@AutoConfiguration
@ConditionalOnClass(WebFilter.class)
@ConditionalOnProperty(prefix = "vex.auth.user-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthUserFilterAutoConfiguration {

    @Bean
    public AuthUserFilter authUserFilter() {
        return new AuthUserFilter();
    }
}
