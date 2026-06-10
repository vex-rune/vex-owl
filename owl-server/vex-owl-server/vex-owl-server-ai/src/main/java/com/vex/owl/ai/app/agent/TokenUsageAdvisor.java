package com.vex.owl.ai.app.agent;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.tools.AgentAdvisor;
import com.vex.owl.ai.domain.event.TokenUsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
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
public class TokenUsageAdvisor implements AgentAdvisor {

    public static final String NAME = "TokenUsageListenerAdvisor";
    public static final int ORDER = 2;

    private static final String FINISH_REASON_STOP = "STOP";
    private static final String FINISH_INFO_STREAM_COMPLETED = "stream completed";

    private static final String KEY_USER_ID = "userId";
    private static final String KEY_SESSION_ID = "sessionId";
    private static final String KEY_PROVIDER = "provider";
    private static final String KEY_EXECUTED = "tokenUsageAdvisor.executed";

    private final EventPublisher eventPublisher;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        Map<String, Object> ctx = chatClientRequest.context();

        // 防重入：已执行过则直接跳过
        if (Boolean.TRUE.equals(ctx.get(KEY_EXECUTED))) {
            return callAdvisorChain.nextCall(chatClientRequest);
        }
        ctx.put(KEY_EXECUTED, true);

        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        ChatResponse chatResponse = response.chatResponse();
        ChatResponseMetadata metadata = chatResponse.getMetadata();
        Usage usage = metadata.getUsage();

        Generation result = chatResponse.getResult();

        if (result == null) {
            log.debug("result:{}", result);
            return response;
        }

        if (FINISH_REASON_STOP.equals(result.getMetadata().getFinishReason())) {
            TokenUsageEvent event = TokenUsageEvent.builder()
                    .userId(getString(ctx, KEY_USER_ID))
                    .sessionId(getString(ctx, KEY_SESSION_ID))
                    .provider(getString(ctx, KEY_PROVIDER))
                    .modelName(metadata.getModel())
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .totalTokens(usage.getTotalTokens())
                    .build();
            log.debug("发送事件 TokenUsageEvent:{}", event);
            eventPublisher.publish("TokenUsageEvent", event);
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
            StreamAdvisorChain streamAdvisorChain) {

        Map<String, Object> ctx = chatClientRequest.context();

        // 防重入：已执行过则直接跳过
        if (Boolean.TRUE.equals(ctx.get(KEY_EXECUTED))) {
            return streamAdvisorChain.nextStream(chatClientRequest);
        }
        ctx.put(KEY_EXECUTED, true);

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
            log.debug("finishReason:{}", FINISH_INFO_STREAM_COMPLETED);

            TokenUsageEvent event = TokenUsageEvent.builder()
                    .userId(getString(ctx, KEY_USER_ID))
                    .sessionId(getString(ctx, KEY_SESSION_ID))
                    .provider(getString(ctx, KEY_PROVIDER))
                    .modelName(modelName.get())
                    .promptTokens(promptTokens.get())
                    .completionTokens(completionTokens.get())
                    .totalTokens(totalTokens.get())
                    .build();
            log.debug("发送事件 TokenUsageEvent:{}", event);
            eventPublisher.publish("TokenUsageEvent", event);
        });
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
}