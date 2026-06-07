package com.vex.owl.ai.app;

import com.vex.owl.ai.domain.context.RunContext;
import com.vex.owl.ai.domain.event.TokenUsageEvent;
import lombok.RequiredArgsConstructor;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Token 使用量监听器
 *
 * <p>监听 AI 调用的 Token 使用量，发送 TokenUsageEvent</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUsageListenerAdvisor implements CallAdvisor, StreamAdvisor {

    /** Advisor 名称 */
    public static final String NAME = "TokenUsageListenerAdvisor";
    
    /** Advisor 顺序 */
    public static final int ORDER = 2;
    
    /** 魔法值 */
    private static final String FINISH_REASON_STOP = "STOP";
    private static final String FINISH_INFO_STREAM_COMPLETED = "stream completed";

    private final ApplicationEventPublisher publisher;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        Map<String, Object> context = chatClientRequest.context();
        ChatResponse chatResponse = response.chatResponse();
        ChatResponseMetadata metadata = chatResponse.getMetadata();
        Usage usage = metadata.getUsage();

        RunContext runContext = RunContext.fromMap(context);

        if (FINISH_REASON_STOP.equals(chatResponse.getResult().getMetadata().getFinishReason())) {
            TokenUsageEvent event = TokenUsageEvent.builder()
                    .tenantId(runContext.getTenantId())
                    .sessionId(runContext.getSessionId())
                    .provider(runContext.getProvider())
                    .modelName(metadata.getModel())
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .totalTokens(usage.getTotalTokens())
                    .build();
            log.info("发送事件 TokenUsageEvent:{}", event);
            publisher.publishEvent(event);
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

    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest chatClientRequest,
            StreamAdvisorChain streamAdvisorChain
    ) {

        AtomicInteger promptTokens = new AtomicInteger(0);
        AtomicInteger completionTokens = new AtomicInteger(0);
        AtomicInteger totalTokens = new AtomicInteger(0);
        AtomicReference<String> modelName = new AtomicReference<>();

        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        return responseFlux.doOnNext(response -> {
            ChatResponse chatResponse = response.chatResponse();
            if (chatResponse != null && chatResponse.getMetadata() != null) {
                Usage usage = chatResponse.getMetadata().getUsage();
                if (usage != null) {
                    promptTokens.addAndGet(usage.getPromptTokens());
                    completionTokens.addAndGet(usage.getCompletionTokens());
                    totalTokens.addAndGet(usage.getTotalTokens());

                    if (modelName.get() == null) {
                        String model = chatResponse.getMetadata().getModel();
                        if (model != null) {
                            modelName.set(model);
                        }
                    }
                }
            }
        }).doOnComplete(() -> {
            log.info("finishReason:{}", FINISH_INFO_STREAM_COMPLETED);


            Map<String, Object> context = chatClientRequest.context();
            RunContext runContext = RunContext.fromMap(context);

            TokenUsageEvent event = TokenUsageEvent.builder()
                    .tenantId(runContext.getTenantId())
                    .sessionId(runContext.getSessionId())
                    .provider(runContext.getProvider())
                    .modelName(modelName.get())
                    .promptTokens(promptTokens.get())
                    .completionTokens(completionTokens.get())
                    .totalTokens(totalTokens.get())
                    .build();
            log.info("发送事件 TokenUsageEvent:{}", event);
            publisher.publishEvent(event);
        });
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
