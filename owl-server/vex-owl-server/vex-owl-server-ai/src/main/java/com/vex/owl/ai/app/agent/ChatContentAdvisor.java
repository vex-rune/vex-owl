//package com.vex.owl.ai.app.agent;
//
//import com.vex.owl.ai.domain.tools.AgentAdvisor;
//import com.vex.owl.ai.domain.event.ChatContentEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.chat.client.ChatClientRequest;
//import org.springframework.ai.chat.client.ChatClientResponse;
//import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
//import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
//import org.springframework.ai.chat.model.ChatResponse;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Flux;
//
//import java.util.Map;
//
///**
// * LLM 响应内容推送 Advisor
// *
// * <p>通过 Spring 事件异步发布 ChatContentEvent：</p>
// * <ul>
// *   <li>非流式 (call)：一次性发布完整响应</li>
// *   <li>流式 (stream)：逐 chunk 实时发布，最后一个 chunk 标记 finish=true</li>
// * </ul>
// *
// * <p>事件由 {@link SseEventBroadcaster} 异步收集，
// * 按 sessionId 推送到 SSE 端点</p>
// */
//@Slf4j
//@Component
//public class ChatContentAdvisor implements AgentAdvisor {
//
//    public static final String NAME = "ChatContentAdvisor";
//    public static final int ORDER = 3;
//
//    private final ApplicationEventPublisher publisher;
//
//    public ChatContentAdvisor(ApplicationEventPublisher publisher) {
//        this.publisher = publisher;
//    }
//
//    @Override
//    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
//        ChatClientResponse response = chain.nextCall(request);
////
////        Map<String, Object> ctx = request.context();
////        ChatResponse chatResponse = response.chatResponse();
////        String text = chatResponse.getResult().getOutput().getText();
////
////        if (text != null && !text.isEmpty()) {
////            publish(ctx, text, false, true);
////        }
//
//        return response;
//    }
//
//    @Override
//    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
//        return chain.nextStream(request)
//                .doOnNext(response -> {
//                    Map<String, Object> ctx = request.context();
//                    ChatResponse chatResponse = response.chatResponse();
//                    String text = chatResponse != null && chatResponse.getResult() != null
//                            ? chatResponse.getResult().getOutput().getText()
//                            : null;
//                    if (text != null && !text.isEmpty()) {
//                        publish(ctx, text, true, false);
//                    }
//                })
//                .doOnComplete(() -> publish(request.context(), "", true, true));
//    }
//
//    @Override
//    public String getName() {
//        return NAME;
//    }
//
//    @Override
//    public int getOrder() {
//        return ORDER;
//    }
//
//    private void publish(Map<String, Object> ctx, String content, boolean stream, boolean finish) {
//        publisher.publishEvent(ChatContentEvent.builder()
//                .userId(ctx.getOrDefault("userId", "").toString())
//                .sessionId(ctx.getOrDefault("sessionId", "").toString())
//                .content(content)
//                .stream(stream)
//                .finish(finish)
//                .build());
//    }
//}
