package com.vex.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 跨域配置类
 * 统一配置跨域规则，无需各服务单独处理
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // 允许所有域名访问（生产环境建议配置具体域名）
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的请求方法
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        
        // 允许的请求头
        corsConfig.setAllowedHeaders(List.of("*"));
        
        // 允许携带认证信息
        corsConfig.setAllowCredentials(true);
        
        // 预检请求的有效期（秒）
        corsConfig.setMaxAge(3600L);
        
        // 暴露的响应头
        corsConfig.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-User-Id"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
