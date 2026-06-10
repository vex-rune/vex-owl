package com.vex.owl.ai.app.chat;

import com.vex.event.CurrentUser;
import com.vex.owl.ai.domain.AiManager;
import com.vex.owl.ai.domain.agent.AgentManager;
import com.vex.owl.ai.domain.agent.SimpleChatAgent;
import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatSessionEntity;
import com.vex.owl.ai.domain.context.RunContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatApp {

    private final FreeModelPropertiesConfig modelProperties;
    private final AiManager aiManager;
    private final AgentManager agentManager;
    private final ChatManager chatManager;

    public String chat(String userId,  String prompt) {
        ChatSessionEntity session = chatManager.createSessionByType(userId, "FREE_CHAT");
        String resolvedSessionId = session.getId();

        RunContext context = RunContext.builder()
                .modelProperties(modelProperties)
                .userId(userId)
                .sessionId(resolvedSessionId)
                .build();

        ChatClient client = aiManager.createClient(context);

        SimpleChatAgent agent = agentManager.getAgent(SimpleChatAgent.class).orElseThrow(() -> new RuntimeException("Agent not found"));


        return agent.call(prompt, client, context);

    }
}
