package com.vex.owl.ai.domain.usage;

import com.vex.owl.ai.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * AI 使用量统计管理器
 *
 * <p>监听各种 AI 使用事件，聚合统计到数据库</p>
 *
 * <h2>功能说明</h2>
 * <ul>
 *   <li>监听 VoiceUsageEvent - 语音合成使用量</li>
 *   <li>监听 ImageUsageEvent - 图像生成使用量</li>
 *   <li>监听 MusicUsageEvent - 音乐生成使用量</li>
 *   <li>监听 TokenUsageEvent - 对话 Token 使用量</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 事件会被自动监听并聚合统计
 * // 无需手动调用，所有 MiniMaxService 调用都会自动触发统计
 * }</pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageRecordManager {

    private final UsageRecordRepository usageRecordRepository;

    /**
     * 监听 TTS 语音合成使用量事件
     */
    @Async
    @EventListener
    @Transactional
    public void onVoiceUsage(VoiceUsageEvent event) {
        AiContextMetadata metadata = event.getMetadata();
        if (metadata == null || metadata.getTenantId() == null) {
            log.warn("VoiceUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        VoiceUsageEvent.VoiceUsageData data = event.getData();
        if (data == null) {
            log.warn("VoiceUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                metadata.getTenantId(),
                LocalDate.now(),
                "VOICE",
                metadata.getAiPlatform(),
                metadata.getAiModel()
        );

        record.addVoiceUsage(
                data.getInputChars() != null ? data.getInputChars().longValue() : 0L,
                data.getOutputDuration() != null ? data.getOutputDuration().longValue() : 0L,
                data.getOutputSize() != null ? data.getOutputSize().longValue() : 0L
        );

        usageRecordRepository.save(record);

        log.debug("VOICE 使用量统计已更新，租户={}, 调用次数={}",
                metadata.getTenantId(), record.getVoiceCallCount());
    }

    /**
     * 监听图像生成使用量事件
     */
    @Async
    @EventListener
    @Transactional
    public void onImageUsage(ImageUsageEvent event) {
        AiContextMetadata metadata = event.getMetadata();
        if (metadata == null || metadata.getTenantId() == null) {
            log.warn("ImageUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        ImageUsageEvent.ImageUsageData data = event.getData();
        if (data == null) {
            log.warn("ImageUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                metadata.getTenantId(),
                LocalDate.now(),
                "IMAGE",
                metadata.getAiPlatform(),
                metadata.getAiModel()
        );

        record.addImageUsage(
                data.getInputChars() != null ? data.getInputChars().longValue() : 0L,
                data.getRequestCount() != null ? data.getRequestCount().longValue() : 0L,
                data.getSuccessCount() != null ? data.getSuccessCount().longValue() : 0L,
                data.getFailedCount() != null ? data.getFailedCount().longValue() : 0L
        );

        usageRecordRepository.save(record);

        log.debug("IMAGE 使用量统计已更新，租户={}, 请求数量={}",
                metadata.getTenantId(), record.getImageRequestCount());
    }

    /**
     * 监听音乐生成使用量事件
     */
    @Async
    @EventListener
    @Transactional
    public void onMusicUsage(MusicUsageEvent event) {
        AiContextMetadata metadata = event.getMetadata();
        if (metadata == null || metadata.getTenantId() == null) {
            log.warn("MusicUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        MusicUsageEvent.MusicUsageData data = event.getData();
        if (data == null) {
            log.warn("MusicUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                metadata.getTenantId(),
                LocalDate.now(),
                "MUSIC",
                metadata.getAiPlatform(),
                metadata.getAiModel()
        );

        record.addMusicUsage(
                data.getInputChars() != null ? data.getInputChars().longValue() : 0L,
                data.getOutputDuration() != null ? data.getOutputDuration().longValue() : 0L,
                data.getOutputSize() != null ? data.getOutputSize().longValue() : 0L
        );

        usageRecordRepository.save(record);

        log.debug("MUSIC 使用量统计已更新，租户={}, 调用次数={}",
                metadata.getTenantId(), record.getMusicCallCount());
    }

    /**
     * 监听对话 Token 使用量事件
     */
    @Async
    @EventListener
    @Transactional
    public void onTokenUsage(TokenUsageEvent event) {
        AiContextMetadata metadata = event.getMetadata();
        if (metadata == null || metadata.getTenantId() == null) {
            log.warn("TokenUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        TokenUsageEvent.TokenUsageData data = event.getData();
        if (data == null) {
            log.warn("TokenUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                metadata.getTenantId(),
                LocalDate.now(),
                "CHAT",
                metadata.getAiPlatform(),
                metadata.getAiModel()
        );

        record.addChatUsage(
                data.getPromptTokens() != null ? data.getPromptTokens().longValue() : 0L,
                data.getCompletionTokens() != null ? data.getCompletionTokens().longValue() : 0L,
                data.getTotalTokens() != null ? data.getTotalTokens().longValue() : 0L
        );

        usageRecordRepository.save(record);

        log.debug("CHAT 使用量统计已更新，租户={}, 总Token数={}",
                metadata.getTenantId(), record.getTotalTokens());
    }

    /**
     * 获取或创建统计记录
     */
    private UsageRecordEntity getOrCreateRecord(String tenantId, LocalDate statDate,
                                                String usageType, String aiPlatform, String modelName) {
        return usageRecordRepository.findByTenantIdAndStatDateAndUsageType(tenantId, statDate, usageType)
                .orElseGet(() -> UsageRecordEntity.builder()
                        .tenantId(tenantId)
                        .statDate(statDate)
                        .usageType(usageType)
                        .aiPlatform(aiPlatform)
                        .modelName(modelName)
                        .build());
    }

    /**
     * 查询租户某日期的使用量
     */
    public UsageRecordEntity getUsageRecord(String tenantId, LocalDate statDate) {
        return usageRecordRepository.findByTenantIdAndStatDateAndUsageType(tenantId, statDate, "VOICE")
                .orElse(null);
    }
}
