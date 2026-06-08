package com.vex.owl.ai.app.agent;

import com.vex.owl.ai.domain.tools.AgentAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class ChatManagerAdvisor implements AgentAdvisor {

    public final MessageChatMemoryAdvisor messageChatMemoryAdvisor;

    public ChatManagerAdvisor(ChatMemory chatMemory) {
        messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        chatClientRequest.context().put(ChatMemory.CONVERSATION_ID, chatClientRequest.context().get("sessionId"));
        return messageChatMemoryAdvisor.adviseCall(chatClientRequest, callAdvisorChain);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        chatClientRequest.context().put(ChatMemory.CONVERSATION_ID, chatClientRequest.context().get("sessionId"));
        return messageChatMemoryAdvisor.adviseStream(chatClientRequest, streamAdvisorChain);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @return
     */
    @Override
    public int getOrder() {
        return messageChatMemoryAdvisor.getOrder();
    }
}
