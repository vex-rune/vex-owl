package com.vex.security.config;

import com.vex.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "vex.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JwtSecurityAutoConfiguration {

    private final JwtProperties jwtProperties;

    public JwtSecurityAutoConfiguration(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "vex.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(
            jwtProperties.getSecret(),
            jwtProperties.getAccessTokenValidity(),
            jwtProperties.getRefreshTokenValidity(),
            jwtProperties.getIssuer()
        );
    }
}