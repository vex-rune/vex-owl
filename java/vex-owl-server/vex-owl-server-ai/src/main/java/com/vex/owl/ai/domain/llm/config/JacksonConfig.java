package com.vex.owl.ai.domain.llm.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 宽松模式配置
 * <p>LLM 输出的 JSON 格式不稳定，启用宽松解析以容忍：
 * <ul>
 *   <li>JSON 中的注释（{@code //} 和 {@code &#47;* *&#47;}）</li>
 *   <li>单引号字段名和值</li>
 *   <li>未加引号的字段名</li>
 *   <li>预期之外的未知字段</li>
 * </ul>
 * </p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        /// 允许未知字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        /// 允许注释
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        /// 允许单引号字段名和值
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        /// 允许未加引号的字段名
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        return mapper;
    }
}
