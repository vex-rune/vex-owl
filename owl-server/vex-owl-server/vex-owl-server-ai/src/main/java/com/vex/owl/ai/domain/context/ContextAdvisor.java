package com.vex.owl.ai.domain.context;

import com.vex.owl.ai.domain.event.TokenUsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 上下文 Advisor
 *
 * <p>负责将 RunContext 存入请求上下文，供后续组件使用</p>
 *
 * <p>设计原则：</p>
 * <ul>
 *   <li>从 context 中取出 RunContext 或创建新的</li>
 *   <li>将 RunContext 存入 chatClientRequest.context() 供后续使用</li>
 *   <li>在响应时更新 RunContext 的状态（如 step）</li>
 * </ul>
 */
@Slf4j
@Component
public class ContextAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * Advisor 名称
     */
    public static final String NAME = "ContextAdvisor";

    /**
     * Advisor 顺序 - 优先执行
     */
    public static final int ORDER = 0;


    /**
     * 魔法值
     */
    private static final String FINISH_REASON_STOP = "STOP";

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        ChatResponse chatResponse = response.chatResponse();
        if (FINISH_REASON_STOP.equals(chatResponse.getResult().getMetadata().getFinishReason())) {
            Map<String, Object> context = chatClientRequest.context();
            RunContext.updateResultByMap(context, chatResponse.getResult().getOutput().getText());
            log.debug("ContextAdvisor.adviseCall update result: {}", chatResponse.getResult().getOutput().getText());
        }

        return response;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
                                                 StreamAdvisorChain streamAdvisorChain) {

        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        return responseFlux.doOnNext(response -> {
            ChatResponse chatResponse = response.chatResponse();
            if (FINISH_REASON_STOP.equals(chatResponse.getResult().getMetadata().getFinishReason())) {
                Map<String, Object> context = chatClientRequest.context();
                RunContext.updateResultByMap(context, chatResponse.getResult().getOutput().getText());
                log.debug("ContextAdvisor.adviseStream update result: {}", chatResponse.getResult().getOutput().getText());
            }
        });
    }

}
