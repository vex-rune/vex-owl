package com.vex.owl.ai.domain.event;

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


@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUsageListenerAdvisor implements CallAdvisor, StreamAdvisor {

    private final ApplicationEventPublisher publisher;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        Map<String, Object> context = chatClientRequest.context();

        ChatResponse chatResponse = response.chatResponse();

        ChatResponseMetadata metadata = chatResponse.getMetadata();

        Usage usage = metadata.getUsage();

        log.info("finishReason:{}", chatResponse.getResult().getMetadata().getFinishReason().toString());

        if ("STOP".equals(chatResponse.getResult().getMetadata().getFinishReason().toString())) {


            TokenUsageEvent event = new TokenUsageEvent(
                    context,
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens(),
                    metadata.getModel()
            );
            log.info("发送事件 TokenUsageEvent:{}", event);
            publisher.publishEvent(event);
        }

        return response;
    }

    @Override
    public String getName() {
        return TokenUsageListenerAdvisor.class.getName();
    }

    @Override
    public int getOrder() {
        return 2;
    }

    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest chatClientRequest,
            StreamAdvisorChain streamAdvisorChain
    ) {
        Map<String, Object> context = chatClientRequest.context();

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
            String finishInfo = "stream completed";
            log.info("finishReason:{}", finishInfo);

            TokenUsageEvent event = new TokenUsageEvent(
                    context,
                    promptTokens.get(),
                    completionTokens.get(),
                    totalTokens.get(),
                    modelName.get()
            );
            log.info("发送事件 TokenUsageEvent:{}", event);
            publisher.publishEvent(event);
        });
    }

}