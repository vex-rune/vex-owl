package com.vex.owl.gateway.config;

import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT配置属性类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT密钥
     */
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    private Long expiration;

    /**
     * HTTP请求头名称
     */
    private String header;

    /**
     * Token前缀
     */
    private String prefix;

    /**
     * 创建签名密钥
     */
    @Bean
    public SecretKey secretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}