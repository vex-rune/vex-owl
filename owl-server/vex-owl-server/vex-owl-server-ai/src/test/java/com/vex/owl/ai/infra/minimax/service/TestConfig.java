package com.vex.owl.ai.infra.minimax.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试配置类
 *
 * <p>提供 Feign 客户端所需的 HttpMessageConverters Bean</p>
 */
@Configuration
public class TestConfig {

    /**
     * 提供 HttpMessageConverters Bean
     *
     * <p>Feign 客户端需要此 Bean 来进行 JSON 序列化和反序列化</p>
     *
     * @param objectMapper Jackson ObjectMapper
     * @return HttpMessageConverters 配置
     */
    @Bean
    public HttpMessageConverters httpMessageConverters(ObjectMapper objectMapper) {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        return new HttpMessageConverters(true, converters);
    }
}
