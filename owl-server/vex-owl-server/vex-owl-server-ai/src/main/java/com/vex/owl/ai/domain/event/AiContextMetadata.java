package com.vex.owl.ai.domain.event;

import lombok.Builder;
import lombok.Data;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 上下文元数据
 *
 * <p>包含 AI 调用的上下文信息，用于追踪和统计</p>
 *
 * <h2>字段说明</h2>
 * <ul>
 *   <li>aiPlatform - 模型平台（如 minimax, openai, deepseek 等）</li>
 *   <li>aiModel - 模型名称（如 speech-2.8-hd, gpt-4 等）</li>
 *   <li>aiType - AI 类型（CHAT-对话, VOICE-语音, IMAGE-图像, MUSIC-音乐）</li>
 *   <li>tenantId - 租户标识</li>
 *   <li>sessionId - 会话标识</li>
 *   <li>messageId - 消息标识</li>
 * </ul>
 */
@Data
@Builder
public class AiContextMetadata {

    /**
     * 模型平台
     * 如：minimax, openai, deepseek, dashscope 等
     */
    private String aiPlatform;

    /**
     * 模型名称
     * 如：speech-2.8-hd, gpt-4, deepseek-chat 等
     */
    private String aiModel;

    /**
     * AI 类型
     * CHAT - 对话
     * VOICE - 语音（TTS/ASR）
     * IMAGE - 图像生成
     * MUSIC - 音乐生成
     */
    private AiType aiType;

    /**
     * 租户标识
     */
    private String tenantId;

    /**
     * 会话标识
     */
    private String sessionId;

    /**
     * 消息标识
     */
    private String messageId;

    /**
     * AI 类型枚举
     */
    public enum AiType {
        CHAT,   // 对话
        VOICE,  // 语音（TTS/ASR）
        IMAGE,  // 图像生成
        MUSIC   // 音乐生成
    }

    /**
     * 从 Map 转换为 AiContextMetadata
     *
     * <p>支持从事件 context 中提取元数据</p>
     *
     * @param map 包含元数据的 Map
     * @return AiContextMetadata 实例
     */
    public static AiContextMetadata fromMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        return AiContextMetadata.builder()
                .aiPlatform(getString(map, "aiPlatform"))
                .aiModel(getString(map, "aiModel"))
                .aiType(parseAiType(getString(map, "aiType")))
                .tenantId(getString(map, "tenantId"))
                .sessionId(getString(map, "sessionId"))
                .messageId(getString(map, "messageId"))
                .build();
    }

    /**
     * 转换为 Map
     *
     * <p>用于存储到事件 context 中</p>
     *
     * @return 包含元数据的 Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (aiPlatform != null) map.put("aiPlatform", aiPlatform);
        if (aiModel != null) map.put("aiModel", aiModel);
        if (aiType != null) map.put("aiType", aiType.name());
        if (tenantId != null) map.put("tenantId", tenantId);
        if (sessionId != null) map.put("sessionId", sessionId);
        if (sessionId != null) map.put(ChatMemory.CONVERSATION_ID, sessionId);
        if (messageId != null) map.put("messageId", messageId);
        return map;
    }

    /**
     * 合并到现有 Map
     *
     * <p>将元数据合并到已有的 Map 中，不会覆盖已存在的值</p>
     *
     * @param target 目标 Map
     * @return 合并后的 Map
     */
    public Map<String, Object> mergeToMap(Map<String, Object> target) {
        if (target == null) {
            target = new HashMap<>();
        }
        toMap().forEach(target::putIfAbsent);
        return target;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private static AiType parseAiType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AiType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
