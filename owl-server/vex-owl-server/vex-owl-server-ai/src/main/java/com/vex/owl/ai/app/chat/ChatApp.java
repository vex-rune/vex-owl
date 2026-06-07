package com.vex.owl.ai.app.chat;

import com.vex.owl.ai.domain.chat.ChatManager;
import com.vex.owl.ai.domain.chat.ChatMessageMemory;
import com.vex.owl.ai.domain.chat.ChatSessionEntity;
import com.vex.owl.ai.domain.llm.factory.ModelProductFactory;
import com.vex.owl.ai.domain.tools.ToolServer;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ChatApp {

    private final FreeModelPropertiesConfig modelProperties;
    private final ModelProductFactory modelProductFactory;
    private final ToolServer toolServer;
    private final ChatMessageMemory chatMessageMemory;
    private final ChatManager chatManager;

    /**
     * 免费对话
     */
    public Flux<String> chat(String tenantId, String prompt) {
        // 1. 获取会话
        ChatSessionEntity session = chatManager.getSession("free_" + tenantId, tenantId);

        // 2. 获取 ChatClient
        ChatClient client = modelProductFactory.getFactory(modelProperties.getProviderCode())
                .createClient(modelProperties);

        // 3. 获取工具
        List<ToolCallback> publicTools = toolServer.getPublicTools();

        // 4. 构建记忆
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMessageMemory).build();

        // 5. 构建上下文
        Map<String, Object> toolContext = Map.of(
                "tenantId", tenantId,
                "sessionId", session.getId(),
                "messageId", UUID.randomUUID().toString()
        );

        Consumer<ChatClient.AdvisorSpec> advisorSpecConsumer = spe -> spe
                .advisors(memoryAdvisor)
                .params(toolContext);

        // 6. 执行对话
        return client.prompt(prompt)
                .toolContext(toolContext)
                .toolCallbacks(publicTools)
                .advisors(memoryAdvisor)
                .advisors(advisorSpecConsumer)
                .system("""
                        你叫"奥沃"，一个由 VEX 技术团队打造的 AI 助手。
                        
                        【核心能力】
                        - 智能对话：能够理解并回答各类问题，提供准确、有用的信息
                        - 任务助手：协助编写代码、分析数据、撰写文档、处理复杂问题
                        - 创意伙伴：帮助头脑风暴、提供创意建议、优化方案
                        - 学习辅导：解释概念、分析案例、提供指导
                        
                        【沟通风格】
                        - 专业友善：用词准确、表达清晰、态度亲切
                        - 主动思考：不仅回答问题，还会主动提供相关建议和延伸思考
                        - 简洁高效：直接给出核心答案，必要时提供详细解释
                        - 诚实可靠：不清楚的问题会如实说明，不编造信息
                        
                        【特殊标记】
                        - 使用(laughs)表示笑声
                        - 使用(sighs)表示叹气
                        - 遇到不确定的问题会说"这个问题我需要思考一下"
                        - 发现错误会主动承认并纠正
                        
                        始终保持专业、友善、有耐心，努力成为用户信赖的 AI 助手。
                        """)
                .stream()
                .content();
    }
}