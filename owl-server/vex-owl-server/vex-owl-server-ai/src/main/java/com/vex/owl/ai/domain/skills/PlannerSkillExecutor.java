package com.vex.owl.ai.domain.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vex.owl.ai.domain.skills.SkillResult.Metadata;
import com.vex.owl.ai.domain.skills.SkillResult.ResultType;
import com.vex.owl.ai.domain.skills.SkillResult.TokenUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 计划技能执行器
 * <p>每次执行构造新实例，注入大模型客户端、工具上下文和工具列表。
 * 调用 {@link #execute(String)} 将用户需求转化为结构化计划。</p>
 */
@Slf4j
public class PlannerSkillExecutor {

    /** 技能名称 */
    public static final String NAME = "planner";

    /** 系统提示词 */
    public static final String SYSTEM_PROMPT = """
            你是一个计划创建者，将用户的需求转化为**单个可执行的计划**。严格以 JSON 格式输出。

            ## 最小工作流程

            在整个工作流程中，以只读模式操作。不要写入或更新文件。

            1. **快速扫描上下文**
               - 阅读对话上下文
               - 阅读用户档案
               - 了解用户习惯
               - 使用 `thinkRecordLog` 记录扫描结果和初步判断

            2. **仅在阻塞时追问**
               - 最多询问 1-2 个问题
               - 只有在没有答案就无法负责任地制定计划时才询问
               - 优先使用选择题形式
               - 如果不确定但没有被阻塞，做出合理假设并继续
               - 使用 `thinkRecordLog` 记录追问原因和用户回答

            3. **制定计划**
               - 以 1 段简短文字描述意图和方法
               - 清楚地列出范围内和范围外的内容
               - 然后提供一个小型行动项清单（默认 6-10 项）
               - 每个清单项应该是一个具体的行动，并在有帮助时提及文件或命令
               - 使项目原子化并有序：发现 → 更改 → 测试 → 发布
               - 动词开头："添加..."、"重构..."、"验证..."、"发布..."
               - 至少包含一个测试验证项和一个边缘案例/风险项
               - 使用 `thinkRecordLog` 记录计划制定过程中的关键决策

            4. **保存结果**
               - 使用 `saveResult` 保存最终计划结果

            5. **仅输出 JSON，不要任何额外文本**

            ## 输出格式（严格 JSON）

            {
              "title": "<1-3句话：我们要做什么，为什么，以及高级方法>",
              "scope": ["包含：...", "排除：..."],
              "actionItems": [
                { "order": 1, "content": "<步骤 1>" },
                { "order": 2, "content": "<步骤 2>" },
                { "order": 3, "content": "<步骤 3>" },
                { "order": 4, "content": "<步骤 4>" },
                { "order": 5, "content": "<步骤 5>" },
                { "order": 6, "content": "<步骤 6>" }
              ],
              "openQuestions": [
                "<问题 1>",
                "<问题 2>",
                "<问题 3>"
              ]
            }

            ## 清单项指南
            好的清单项：
            - 指向可能的文件或模块
            - 指定具体的验证方式
            - 相关时包含安全发布说明

            避免：
            - 模糊的步骤
            - 过多的微步骤
            - 编写代码片段（保持计划与实现无关）
            """;

    private final ChatClient chatClient;
    private final Map<String, Object> toolContext;
    private final List<ToolCallback> tools;
    private final ChatMemory chatMemory;

    /**
     * @param chatClient  已绑定模型的 ChatClient
     * @param toolContext 工具上下文（tenantId / sessionId / messageId）
     * @param tools       本次执行可用的 ToolCallback 列表
     * @param chatMemory  对话记忆（支持多轮上下文，可为 null）
     */
    public PlannerSkillExecutor(@NonNull ChatClient chatClient, @NonNull Map<String, Object> toolContext, @NonNull List<ToolCallback> tools, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.toolContext = Map.copyOf(toolContext);
        this.tools = List.copyOf(tools);
        this.chatMemory = chatMemory;
    }

    /**
     * 执行计划技能
     *
     * @param userMessage 用户请求内容
     * @return 包含 Plan 的技能执行结果
     */
    public SkillResult<Plan> execute(@NonNull String userMessage) {
        PlanCollector planCollector = new PlanCollector();

        List<ToolCallback> allTools = new ArrayList<>(tools);
        allTools.addAll(List.of(ToolCallbacks.from(planCollector)));

        ChatResponse response;
        try {
            var prompt = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .toolContext(toolContext)
                    .toolCallbacks(allTools);

            if (chatMemory != null) {
                prompt = prompt.advisors(MessageChatMemoryAdvisor.builder(chatMemory).build());
            }

            response = prompt.call().chatResponse();
        } catch (Exception e) {
            log.error("LLM 调用失败, userMessage={}", userMessage, e);
            return SkillResult.<Plan>builder()
                    .code(SkillResult.CODE_ERROR)
                    .type(ResultType.TEXT)
                    .metadata(buildMetadata(null, null))
                    .build();
        }

        Plan plan = planCollector.plan;

        Metadata metadata = buildMetadata(
                response.getMetadata() != null ? response.getMetadata().getUsage() : null,
                response.getMetadata() != null ? response.getMetadata().getModel() : null);

        return SkillResult.<Plan>builder()
                .code(SkillResult.CODE_SUCCESS)
                .type(ResultType.TASK)
                .metadata(metadata)
                .data(plan)
                .build();
    }

    private Metadata buildMetadata(Usage usage, String modelName) {
        TokenUsage tokenUsage = null;
        if (usage != null) {
            tokenUsage = TokenUsage.builder()
                    .promptTokens((int) usage.getPromptTokens())
                    .completionTokens((int) usage.getCompletionTokens())
                    .totalTokens((int) usage.getTotalTokens())
                    .build();
        }
        return Metadata.builder()
                .taskId((String) toolContext.getOrDefault("messageId", ""))
                .sessionId((String) toolContext.getOrDefault("sessionId", ""))
                .messageId((String) toolContext.getOrDefault("messageId", ""))
                .skillName(NAME)
                .tenantId((String) toolContext.getOrDefault("tenantId", ""))
                .tokenUsage(tokenUsage)
                .modelName(modelName)
                .build();
    }

    /**
     * 计划结果收集器
     * <p>作为 Tool 注册给 LLM，LLM 调用 {@code saveResult(Plan)} 时将计划回传至内存。</p>
     */
    public static class PlanCollector {

        @Getter
        Plan plan;

        @Tool(name = "saveResult", description = "保存计划结果")
        public String saveResult(Plan plan) {
            this.plan = plan;
            return "success";
        }
    }

    /**
     * 计划
     * <p>完整的计划输出：标题、范围、行动项清单、开放问题。</p>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Plan {
        /// 计划标题/意图描述
        private String title;
        /// 范围（包含/排除）
        private List<String> scope;
        /// 行动项列表
        private List<PlanItem> actionItems;
        /// 开放问题列表
        private List<String> openQuestions;
    }

    /**
     * 计划项
     * <p>计划中的单条可执行步骤。</p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanItem {
        /// 序号
        private int order;
        /// 行动内容
        private String content;
    }
}
