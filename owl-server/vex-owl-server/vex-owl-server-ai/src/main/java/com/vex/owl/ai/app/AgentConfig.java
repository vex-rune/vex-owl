package com.vex.owl.ai.app;

import com.vex.owl.ai.app.chat.FreeModelPropertiesConfig;
import com.vex.owl.ai.domain.agent.SimplAgent;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean(name = "simplAgent")
    public SimplAgent simplAgent(ModelProductFactory modelProductFactory, FreeModelPropertiesConfig modelProperties) {
        ChatClient client = modelProductFactory.getFactory(modelProperties.getProviderCode())
                .createClient(modelProperties);
        return SimplAgent.builder().client(client).build();
    }
}
