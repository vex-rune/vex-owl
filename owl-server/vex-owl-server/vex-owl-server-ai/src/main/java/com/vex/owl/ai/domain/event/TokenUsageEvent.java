package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Token 使用量事件
 *
 * <p>记录 AI 调用的 token 使用情况</p>
 *
 * <h2>事件类型</h2>
 * <ul>
 *   <li>CHAT 对话：promptTokens=输入token, completionTokens=输出token, totalTokens=总token</li>
 *   <li>VOICE 语音：promptTokens=输入字符数, completionTokens=输出时长(ms), totalTokens=输出大小(bytes)</li>
 *   <li>IMAGE 图像：promptTokens=输入字符数, completionTokens=请求数量, totalTokens=成功数量</li>
 *   <li>MUSIC 音乐：promptTokens=输入字符数, completionTokens=输出时长(ms), totalTokens=输出大小(bytes)</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * TokenUsageEvent event = new TokenUsageEvent(
 *     AiContextMetadata.builder()
 *         .aiPlatform("minimax")
 *         .aiModel("speech-2.8-hd")
 *         .aiType(AiContextMetadata.AiType.VOICE)
 *         .sessionId("session-123")
 *         .build(),
 *     TokenUsageData.builder()
 *         .promptTokens(25)
 *         .completionTokens(3500)
 *         .totalTokens(56000)
 *         .build()
 * );
 *
 * eventPublisher.publishEvent(event);
 * }</pre>
 *
 * @see AiUsageEvent
 * @see TokenUsageData
 */
@Getter
@Setter
public class TokenUsageEvent extends AiUsageEvent<TokenUsageEvent.TokenUsageData> {

    /**
     * 构造函数
     *
     * @param metadata AI 上下文元数据
     * @param data     Token 使用量数据
     */
    public TokenUsageEvent(AiContextMetadata metadata, TokenUsageData data) {
        super(metadata, data, Instant.now());
    }

    /**
     * 构造函数（保留兼容性）
     *
     * @param context         旧版 context Map（会被转换为 metadata）
     * @param promptTokens    输入 token 数
     * @param completionTokens 输出 token 数
     * @param totalTokens     总 token 数
     * @param modelName       模型名称
     */
    @Deprecated
    public TokenUsageEvent(Map<String, Object> context,
                          Integer promptTokens,
                          Integer completionTokens,
                          Integer totalTokens,
                          String modelName) {
        super(
                AiContextMetadata.fromMap(context),
                TokenUsageData.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(totalTokens)
                        .build(),
                Instant.now()
        );

        if (getMetadata() != null && modelName != null) {
            getMetadata().setAiModel(modelName);
        }
    }

    @Override
    public String getEventType() {
        return "TOKEN_USAGE";
    }

    /**
     * Token 使用量数据
     */
    @Getter
    @Builder
    @Setter
    public static class TokenUsageData {
        /**
         * 输入 token 数（或等价值）
         */
        private Integer promptTokens;

        /**
         * 输出 token 数（或等价值）
         */
        private Integer completionTokens;

        /**
         * 总 token 数（或等价值）
         */
        private Integer totalTokens;
    }
}
