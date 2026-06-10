package com.vex.security.auth;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 当前用户解析器自动配置
 */
@AutoConfiguration
public class CurrentResolverAutoConfiguration {

    @Bean
    public CurrentResolverImpl currentResolver() {
        return new CurrentResolverImpl();
    }
}