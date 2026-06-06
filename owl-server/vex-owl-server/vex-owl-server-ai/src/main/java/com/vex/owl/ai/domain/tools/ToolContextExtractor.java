package com.vex.owl.ai.domain.tools;

import java.util.Optional;

import org.springframework.ai.chat.model.ToolContext;

/**
 * ToolContext 上下文提取器（单例）
 * <p>从 Spring AI 的 ToolContext 中提取租户ID、会话ID、消息ID等上下文信息。
 * 各 Tool 类通过组合方式使用此提取器，无需继承。</p>
 *
 * <h3>约定</h3>
 * <p>所有值由应用层通过 {@code .toolContext(Map.of(...))} 注入，大模型不可见，
 * 防止跨租户数据泄露。</p>
 */
public class ToolContextExtractor {

    private static final ToolContextExtractor INSTANCE = new ToolContextExtractor();

    private ToolContextExtractor() {
    }

    public static ToolContextExtractor getInstance() {
        return INSTANCE;
    }

    /**
     * 提取租户ID
     *
     * @param toolContext Spring AI 自动注入的工具上下文
     * @return 租户ID的 Optional，未设置时返回 {@link Optional#empty()}
     */
    public Optional<String> getTenantId(ToolContext toolContext) {
        return getString(toolContext, "tenantId");
    }

    /**
     * 提取会话ID
     *
     * @param toolContext Spring AI 自动注入的工具上下文
     * @return 会话ID的 Optional，未设置时返回 {@link Optional#empty()}
     */
    public Optional<String> getSessionId(ToolContext toolContext) {
        return getString(toolContext, "sessionId");
    }

    /**
     * 提取消息ID
     *
     * @param toolContext Spring AI 自动注入的工具上下文
     * @return 消息ID的 Optional，未设置时返回 {@link Optional#empty()}
     */
    public Optional<String> getMessageId(ToolContext toolContext) {
        return getString(toolContext, "messageId");
    }

    private Optional<String> getString(ToolContext toolContext, String key) {
        if (toolContext == null || toolContext.getContext() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(toolContext.getContext().get(key))
                .map(Object::toString);
    }
}
