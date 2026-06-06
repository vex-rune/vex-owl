package com.vex.owl.ai.app.chat;

import com.vex.owl.ai.domain.llm.repo.ModelProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vex.free-model-properties")
public class FreeModelPropertiesConfig implements ModelProperties {

    private String providerCode;
    private String apiKey;
    private String modelName;
    private String baseUrl;
}
