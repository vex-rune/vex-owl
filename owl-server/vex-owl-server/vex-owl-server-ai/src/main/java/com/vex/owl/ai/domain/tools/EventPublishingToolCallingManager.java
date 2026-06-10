package com.vex.owl.ai.domain.tools;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.event.ToolCallRequestEvent;
import com.vex.owl.ai.domain.event.ToolCallResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于事件发布的工具调用管理器
 *
 * <p>实现 {@link ToolCallingManager} 接口，内部委托 {@link DefaultToolCallingManager}，
 * 在工具执行前后通过 {@link EventPublisher} 发布事件。</p>
 *
 * <p>主要职责：
 * <ul>
 *   <li>从 ChatResponse 中收集所有工具调用请求</li>
 *   <li>在工具执行前发布 ToolCallRequestEvent</li>
 *   <li>在工具执行后发布 ToolCallResultEvent</li>
 *   <li>最后一个工具调用结果标记 finish=true</li>
 * </ul>
 */
@Slf4j
public class EventPublishingToolCallingManager implements ToolCallingManager {

    /** 从 ToolContext 中获取用户ID */
    private static final String KEY_USER_ID = "userId";
    /** 从 ToolContext 中获取会话ID */
    private static final String KEY_SESSION_ID = "sessionId";
    /** 从 ToolContext 中获取提供商 */
    private static final String KEY_PROVIDER = "provider";
    /** 从 ToolContext 中获取模型名称 */
    private static final String KEY_MODEL = "model";
    /** 上下文中记录已处理的 toolCallId 集合的键 */
    private static final String KEY_PROCESSED_TOOL_CALL_IDS = "processedToolCallIds";

    /** 委托的工具调用管理器，实际执行工具调用逻辑 */
    private final DefaultToolCallingManager delegate;
    /** 事件发布器，用于发布工具调用事件 */
    private final EventPublisher eventPublisher;

    /**
     * 构造函数
     *
     * @param delegate 实际执行工具的委托管理器
     * @param eventPublisher 事件发布器
     */
    public EventPublishingToolCallingManager(DefaultToolCallingManager delegate,
                                             EventPublisher eventPublisher) {
        this.delegate = delegate;
        this.eventPublisher = eventPublisher;
        log.info("EventPublishingToolCallingManager 初始化完成，委托管理器: {}",
                delegate.getClass().getSimpleName());
    }

    /**
     * 解析工具定义
     *
     * <p>直接委托给默认实现，不做任何处理。</p>
     *
     * @param chatOptions 聊天选项，包含工具定义
     * @return 解析后的工具定义列表
     */
    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        log.debug("resolveToolDefinitions 开始，chatOptions: {}",
                chatOptions != null ? chatOptions.getClass().getSimpleName() : "null");
        List<ToolDefinition> result = delegate.resolveToolDefinitions(chatOptions);
        log.debug("resolveToolDefinitions 完成，返回工具数量: {}", result.size());
        return result;
    }

    /**
     * 执行工具调用并发布事件
     *
     * <p>核心方法，处理流程：
     * <ol>
     *   <li>从 Prompt 中提取上下文信息</li>
     *   <li>收集 ChatResponse 中的所有工具调用请求</li>
     *   <li>为每个工具调用发布 BEFORE 事件</li>
     *   <li>委托给默认管理器执行实际调用</li>
     *   <li>为每个执行结果发布 AFTER 事件（最后一个标记 finish=true）</li>
     * </ol>
     *
     * @param prompt 提示词，包含上下文信息
     * @param chatResponse AI 响应，可能包含工具调用请求
     * @return 工具执行结果，包含对话历史
     */
    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        // ===== 1. 提取上下文信息 =====
        Map<String, Object> ctx = extractContext(prompt);
        String userId = getString(ctx, KEY_USER_ID);
        String sessionId = getString(ctx, KEY_SESSION_ID);
        String provider = getString(ctx, KEY_PROVIDER);
        String model = getString(ctx, KEY_MODEL);

        log.debug("executeToolCalls 开始 | userId={} | sessionId={} | provider={} | model={}",
                userId, sessionId, provider, model);

        // ===== 2. 收集所有工具调用请求 =====
        List<AssistantMessage.ToolCall> allToolCalls = collectAllToolCalls(chatResponse);
        log.debug("收集到工具调用请求数量: {}", allToolCalls.size());

        if (allToolCalls.isEmpty()) {
            log.debug("没有检测到工具调用，直接执行委托");
            return delegate.executeToolCalls(prompt, chatResponse);
        }

        // ===== 2.5 检查重复处理 =====
        @SuppressWarnings("unchecked")
        java.util.Set<String> processedIds = (java.util.Set<String>) ctx.get(KEY_PROCESSED_TOOL_CALL_IDS);
        if (processedIds == null) {
            processedIds = new java.util.HashSet<>();
            ctx.put(KEY_PROCESSED_TOOL_CALL_IDS, processedIds);
        }

        // 检查是否有重复的 toolCallId
        for (AssistantMessage.ToolCall tc : allToolCalls) {
            if (processedIds.contains(tc.id())) {
                log.error("检测到重复处理的 toolCallId | toolCallId={} | toolName={} | userId={} | sessionId={}",
                        tc.id(), tc.name(), userId, sessionId);
            }
        }

        // 标记这些 toolCallId 已处理
        for (AssistantMessage.ToolCall tc : allToolCalls) {
            processedIds.add(tc.id());
        }

        // ===== 3. 发布 BEFORE 事件 =====
        log.debug("开始发布 ToolCallRequestEvent，数量: {}", allToolCalls.size());
        for (int i = 0; i < allToolCalls.size(); i++) {
            AssistantMessage.ToolCall tc = allToolCalls.get(i);
            log.debug("发布 BEFORE 事件 [{}/{}] | toolCallId={} | toolName={} | arguments={}",
                    i + 1, allToolCalls.size(), tc.id(), tc.name(), tc.arguments());
            eventPublisher.publish("ToolCallRequestEvent", ToolCallRequestEvent.builder()
                    .userId(userId)
                    .sessionId(sessionId)
                    .provider(provider)
                    .modelName(model)
                    .toolCallId(tc.id())
                    .toolName(tc.name())
                    .arguments(tc.arguments())
                    .build());
        }

        // ===== 4. 执行工具调用 =====
        ToolExecutionResult result;
        try {
            log.debug("开始委托执行工具调用");
            result = delegate.executeToolCalls(prompt, chatResponse);
            log.debug("委托执行完成，结果消息数: {}", result.conversationHistory().size());
        } catch (Exception e) {
            log.error("工具调用执行失败 | error={}", e.getMessage(), e);

            // 发布失败事件
            log.debug("发布失败 ToolCallResultEvent，数量: {}", allToolCalls.size());
            for (int i = 0; i < allToolCalls.size(); i++) {
                AssistantMessage.ToolCall tc = allToolCalls.get(i);
                log.warn("发布失败事件 [{}/{}] | toolCallId={} | toolName={}",
                        i + 1, allToolCalls.size(), tc.id(), tc.name());
                eventPublisher.publish("ToolCallResultEvent", ToolCallResultEvent.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .provider(provider)
                        .modelName(model)
                        .toolCallId(tc.id())
                        .toolName(tc.name())
                        .result("执行失败: " + e.getMessage())
                        .finish(true)
                        .build());
            }
            throw e;
        }

        // ===== 5. 发布 AFTER 事件 =====
        List<ToolResponseMessage> toolResponses = result.conversationHistory().stream()
                .filter(m -> m.getMessageType() == org.springframework.ai.chat.messages.MessageType.TOOL)
                .map(m -> (ToolResponseMessage) m)
                .toList();

        // 计算总响应数，用于判断是否是最后一个
        int totalResponses = toolResponses.stream()
                .mapToInt(m -> m.getResponses().size())
                .sum();

        log.debug("开始发布 ToolCallResultEvent | 总响应数: {} | 消息块数: {}",
                totalResponses, toolResponses.size());

        int emittedCount = 0;
        int blockIndex = 0;
        for (ToolResponseMessage msg : toolResponses) {
            blockIndex++;
            int responseIndex = 0;
            for (var response : msg.getResponses()) {
                emittedCount++;
                responseIndex++;
                // 最后一个响应标记 finish=true，表示这批工具调用全部完成
                boolean isLast = (emittedCount == totalResponses);

                log.debug("发布 AFTER 事件 [块{}/{} 响应{}/{}] | toolCallId={} | toolName={} | result长度={} | finish={}",
                        blockIndex, toolResponses.size(),
                        responseIndex, msg.getResponses().size(),
                        response.id(), response.name(),
                        response.responseData() != null ? response.responseData().length() : 0,
                        isLast);

                eventPublisher.publish("ToolCallResultEvent", ToolCallResultEvent.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .provider(provider)
                        .modelName(model)
                        .toolCallId(response.id())
                        .toolName(response.name())
                        .result(response.responseData())
                        .finish(isLast)
                        .build());
            }
        }

        log.debug("executeToolCalls 完成 | userId={} | sessionId={} | 发布事件数={}",
                userId, sessionId, emittedCount);

        return result;
    }

    /**
     * 收集所有 Generation 中的工具调用请求
     *
     * <p>遍历 ChatResponse 的所有 Generation，提取其中的 ToolCall。
     * 一个 ChatResponse 可能包含多个 Generation，每个都可能有工具调用。</p>
     *
     * @param chatResponse AI 响应
     * @return 所有工具调用列表
     */
    private List<AssistantMessage.ToolCall> collectAllToolCalls(ChatResponse chatResponse) {
        List<AssistantMessage.ToolCall> allToolCalls = new ArrayList<>();

        // 空值检查
        if (chatResponse == null) {
            log.debug("collectAllToolCalls: chatResponse 为空");
            return allToolCalls;
        }

        if (chatResponse.getResults() == null) {
            log.debug("collectAllToolCalls: chatResponse.getResults() 为空");
            return allToolCalls;
        }

        log.debug("collectAllToolCalls: 开始遍历 Generation，数量: {}", chatResponse.getResults().size());

        int generationIndex = 0;
        for (var generation : chatResponse.getResults()) {
            generationIndex++;

            // 检查输出是否为 AssistantMessage
            if (!(generation.getOutput() instanceof AssistantMessage am)) {
                log.debug("collectAllToolCalls [Generation {}/{}]: 输出不是 AssistantMessage，跳过",
                        generationIndex, chatResponse.getResults().size());
                continue;
            }

            // 检查是否有工具调用
            if (am.getToolCalls() == null || am.getToolCalls().isEmpty()) {
                log.debug("collectAllToolCalls [Generation {}/{}]: 没有工具调用",
                        generationIndex, chatResponse.getResults().size());
                continue;
            }

            // 添加工具调用
            int toolCallCount = am.getToolCalls().size();
            allToolCalls.addAll(am.getToolCalls());
            log.debug("collectAllToolCalls [Generation {}/{}]: 发现 {} 个工具调用",
                    generationIndex, chatResponse.getResults().size(), toolCallCount);

            // 详细日志每个工具调用
            for (int i = 0; i < am.getToolCalls().size(); i++) {
                AssistantMessage.ToolCall tc = am.getToolCalls().get(i);
                log.trace("  ToolCall[{}]: id={}, name={}",
                        i, tc.id(), tc.name());
            }
        }

        log.debug("collectAllToolCalls: 完成，共收集 {} 个工具调用", allToolCalls.size());
        return allToolCalls;
    }

    /**
     * 从 Prompt 中提取上下文信息
     *
     * <p>从 Prompt 的 Options 中获取 ToolContext，ToolContext 包含 userId、sessionId 等信息。
     * 这些信息通过 ChatClient 的 toolContext 方法设置。</p>
     *
     * @param prompt 提示词
     * @return 上下文 Map，如果提取失败返回空 Map
     */
    private Map<String, Object> extractContext(Prompt prompt) {
        if (prompt == null) {
            log.warn("extractContext: prompt 为空");
            return new java.util.HashMap<>();
        }

        if (prompt.getOptions() == null) {
            log.debug("extractContext: prompt.getOptions() 为空");
            return new java.util.HashMap<>();
        }

        if (!(prompt.getOptions() instanceof ToolCallingChatOptions options)) {
            log.debug("extractContext: prompt.getOptions() 不是 ToolCallingChatOptions");
            return new java.util.HashMap<>();
        }

        Map<String, Object> ctx = options.getToolContext();
        if (ctx == null) {
            log.debug("extractContext: toolContext 为空");
            return new java.util.HashMap<>();
        }

        // 确保返回可变 Map，因为后续需要添加 processedToolCallIds
        if (ctx instanceof java.util.HashMap) {
            log.debug("extractContext: 成功提取上下文，键数量: {}", ctx.size());
            return ctx;
        }

        log.debug("extractContext: 成功提取上下文（包装为可变 Map），键数量: {}", ctx.size());
        return new java.util.HashMap<>(ctx);
    }

    /**
     * 安全获取字符串值
     *
     * <p>从 Map 中获取指定键的值，如果不存在或为 null 返回空字符串。</p>
     *
     * @param map 源 Map
     * @param key 键
     * @return 值字符串，null 安全
     */
    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            log.trace("getString: key={} 不存在或为 null", key);
            return "";
        }
        String result = value.toString();
        log.trace("getString: key={}, value={}", key, result);
        return result;
    }
}