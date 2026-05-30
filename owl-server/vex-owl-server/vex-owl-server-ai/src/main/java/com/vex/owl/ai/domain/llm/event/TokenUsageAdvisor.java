package com.vex.owl.ai.domain.llm.event;

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


@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUsageAdvisor implements CallAdvisor, StreamAdvisor {

    private final ApplicationEventPublisher publisher;

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest,
                                         CallAdvisorChain callAdvisorChain) {
        ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);

        Map<String, Object> context = chatClientRequest.context();

        ChatResponse chatResponse = response.chatResponse();

        ChatResponseMetadata metadata = chatResponse.getMetadata();

        Usage usage = metadata.getUsage();

        TokenUsageEvent event = new TokenUsageEvent(
                context,
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens(),
                metadata.getModel()
        );
        log.info("发送事件 TokenUsageEvent:{}", event);
        publisher.publishEvent(event);

        return response;
    }

    @Override
    public String getName() {
        return TokenUsageAdvisor.class.getName();
    }

    @Override
    public int getOrder() {
        return 2;
    }

    public Flux<ChatClientResponse> adviseStream(
            ChatClientRequest chatClientRequest,
            StreamAdvisorChain streamAdvisorChain
    ) {

        return streamAdvisorChain.nextStream(chatClientRequest)
                .doOnComplete(() -> {
                    // 流完成后，你能拿到最终的 token 消耗
                    // 注意：这里拿不到，必须 collect 之后
                })
                // 正确做法：把流收集起来，从最后一个元素取 usage
                .collectList()
                .map(responses -> {
                    if (responses.isEmpty()) return null;

                    // 取最后一个响应
                    ChatClientResponse last = responses.get(responses.size() - 1);

                    // 取 token 消耗（Spring AI 标准）
                    ChatResponseMetadata metadata = last.chatResponse().getMetadata();
                    Usage usage = metadata.getUsage();

                    if (usage != null) {
                        Map<String, Object> context = last.context();

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
                    return responses;
                })
                .flatMapMany(Flux::fromIterable); // 转回 Flux 不影响返回值
    }
}