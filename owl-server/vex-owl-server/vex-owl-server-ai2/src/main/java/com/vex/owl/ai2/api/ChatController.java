package com.vex.owl.ai2.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.metadata.ThinkingUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai2/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient dashscopeChatClient;

    /**
     * 同步对话
     */
    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        log.info("收到对话请求: {}", request.prompt());

        String response = dashscopeChatClient.prompt()
                .messages(new UserMessage(request.prompt()))
                .call()
                .content();

        log.info("AI 响应: {}", response);
        return response;
    }

    /**
     * 流式对话
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        log.info("收到流式对话请求: {}", request.prompt());

        return dashscopeChatClient.prompt()
                .messages(new UserMessage(request.prompt()))
                .advisors(new SimpleLoggerAdvisor())
                .stream()
                .content();
    }

    /**
     * 带历史记录的对话
     */
    @PostMapping("/chat-with-history")
    public String chatWithHistory(@RequestBody ChatWithHistoryRequest request) {
        log.info("收到带历史记录的对话请求");

        var prompt = dashscopeChatClient.prompt()
                .messages(convertToMessages(request.history()))
                .messages(new UserMessage(request.prompt()))
                .call();

        return prompt.content();
    }

    /**
     * 使用系统提示词
     */
    @PostMapping("/chat-with-system")
    public String chatWithSystem(@RequestBody ChatWithSystemRequest request) {
        log.info("收到带系统提示词的对话请求");

        String response = dashscopeChatClient.prompt()
                .messages(new SystemMessage(request.systemPrompt()))
                .messages(new UserMessage(request.prompt()))
                .call()
                .content();

        return response;
    }

    private List<org.springframework.ai.chat.messages.Message> convertToMessages(List<ChatMessage> history) {
        return history.stream()
                .map(msg -> switch (msg.role()) {
                    case "user" -> new UserMessage(msg.content());
                    case "assistant" -> new AssistantMessage(msg.content());
                    default -> throw new IllegalArgumentException("Unknown role: " + msg.role());
                })
                .toList();
    }

    public record ChatRequest(String prompt) {}

    public record ChatWithHistoryRequest(String prompt, List<ChatMessage> history) {}

    public record ChatWithSystemRequest(String systemPrompt, String prompt) {}

    public record ChatMessage(String role, String content) {}
}