package com.vex.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "vex.jwt")
public class JwtProperties {

    private String secret = "defaultSecretKeyForDevEnvironmentOnly12345";

    private long accessTokenValidity = 3600;

    private long refreshTokenValidity = 604800;

    private String issuer = "vex-owl";

    private String header = "Authorization";

    private String prefix = "Bearer ";

    private boolean enabled = true;
}