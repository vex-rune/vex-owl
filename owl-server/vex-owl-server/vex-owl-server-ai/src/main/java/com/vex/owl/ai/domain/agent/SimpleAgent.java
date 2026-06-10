package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Component
public class SimpleAgent extends BaseAgent {

    private static final String SYSTEM_PROMPT = "You are a helpful assistant.";

    public SimpleAgent(List<ToolCallback> tools) {
        super("SimpleAgent", "通用助手", tools);
    }

    @Override
    public String type() {
        return "node";
    }

    @Override
    protected String getSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    @Override
    public String call(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext).call().content();
    }

    @Override
    public Flux<String> stream(String input, ChatClient client, RunContext runContext) {
        return spec(input, client, runContext).stream().content();
    }
}
