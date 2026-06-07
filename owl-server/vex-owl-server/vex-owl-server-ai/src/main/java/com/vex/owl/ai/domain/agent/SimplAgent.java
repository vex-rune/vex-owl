package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Slf4j
public class SimplAgent implements Agent {

    private final ChatClient client;
    private final String system;
    private final String name;
    private final String description;
    private final Consumer<ChatClient.PromptSystemSpec> systemSpecConsumer;
    private final Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer;

    private SimplAgent(ChatClient client, String system, String name, String description,
                       Consumer<ChatClient.PromptSystemSpec> systemSpecConsumer,
                       Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer) {
        this.client = client;
        this.system = system;
        this.name = name;
        this.description = description;
        this.systemSpecConsumer = systemSpecConsumer;
        this.advisorSpecConsumer = advisorSpecConsumer;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public AgentDefinition getDefinition() {
        return new AgentDefinition(name, description);
    }

    @Override
    public AgentRequest prompt(String input) {
        return new AgentRequestImpl(this, input);
    }

    private void applyConfig(ChatClient.ChatClientRequestSpec spec) {
        if (systemSpecConsumer != null) {
            spec.system(systemSpecConsumer);
        } else if (system != null && !system.isBlank()) {
            spec.system(system);
        }
        if (advisorSpecConsumer != null) {
            spec.advisors(advisorSpecConsumer);
        }
    }

    public static class Builder {

        private ChatClient client;
        private String system = "";
        private String name = "SimplAgent";
        private String description = "A simple agent";
        private Consumer<ChatClient.PromptSystemSpec> systemSpecConsumer;
        private Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer;

        public Builder client(ChatClient client) {
            this.client = client;
            return this;
        }

        public Builder system(String system) {
            this.system = system;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder systemSpecConsumer(Consumer<ChatClient.PromptSystemSpec> consumer) {
            this.systemSpecConsumer = consumer;
            return this;
        }

        public Builder advisorSpecConsumer(Consumer<ChatClient.AdvisorSpec> consumer) {
            this.advisorSpecConsumer = consumer;
            return this;
        }

        public SimplAgent build() {
            return new SimplAgent(client, system, name, description, systemSpecConsumer, advisorSpecConsumer);
        }
    }

    /**
     * 默认请求构建器：应用 Agent 模板配置 + 追加 per-request messages + 执行
     */
    private static class AgentRequestImpl implements AgentRequest {

        private final SimplAgent agent;
        private final String input;
        private final List<Message> messages = new ArrayList<>();
        private final List<ToolCallback> tools = new ArrayList<>();

        AgentRequestImpl(SimplAgent agent, String input) {
            this.agent = agent;
            this.input = input;
        }

        @Override
        public AgentRequest assistantMessage(String message) {
            this.messages.add(new AssistantMessage(message));
            return this;
        }

        @Override
        public AgentRequest tool(ToolCallback... tool) {
            this.tools.addAll(List.of(tool));
            return this;
        }

        @Override
        public String call(RunContext runContext) {
            ChatClient.ChatClientRequestSpec spec = agent.getClient().prompt(input);
            agent.applyConfig(spec);
            if (!tools.isEmpty()) {
                spec.toolCallbacks(tools);
            }
            if (!messages.isEmpty()) {
                spec.messages(messages);
            }
            String content = spec.toolContext(runContext.toMap()).call().content();
            log.info("SimplAgent call content: {}", content);
            return content;
        }

        @Override
        public Flux<String> stream(RunContext runContext) {
            ChatClient.ChatClientRequestSpec spec = agent.getClient().prompt(input);
            agent.applyConfig(spec);
            if (!tools.isEmpty()) {
                spec.toolCallbacks(tools);
            }
            if (!messages.isEmpty()) {
                spec.messages(messages);
            }
            return spec.toolContext(runContext.toMap()).stream().content();
        }
    }
}
