package com.vex.owl.ai.domain.agent;

import com.vex.owl.ai.domain.context.RunContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;

/**
 * Agent 基类
 *
 * <p>提供公共的 spec() 构建逻辑，子类只需定义 system prompt、tools、advisors。</p>
 */
public abstract class BaseAgent implements Agent<String> {

    protected final String name;
    protected final String description;
    protected final List<ToolCallback> tools;
    protected final List<Advisor> advisors;

    protected BaseAgent(String name, String description,
                        List<ToolCallback> tools, List<Advisor> advisors) {
        this.name = name;
        this.description = description;
        this.tools = tools;
        this.advisors = advisors;
    }

    @Override
    public AgentDefinition getDefinition() {
        return new AgentDefinition(type(), name, description);
    }

    public abstract String type();

    protected ChatClient.ChatClientRequestSpec spec(String input, ChatClient client, RunContext runContext) {
        Map<String, Object> contextMap = runContext.toMap();

        ChatClient.ChatClientRequestSpec spec = client
                .prompt(input)
                .toolContext(contextMap)
                .system(getSystemPrompt());

        if (!tools.isEmpty()) {
            spec.toolCallbacks(tools);
        }

        if (!advisors.isEmpty()) {
            spec.advisors(p -> p.advisors(advisors).params(contextMap));
        }

        return spec;
    }

    protected abstract String getSystemPrompt();
}
