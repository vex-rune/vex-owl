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
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageRecordManager {

    private final UsageRecordRepository usageRecordRepository;

    @Async
    @EventListener
    @Transactional
    public void onVoiceUsage(VoiceUsageEvent event) {
        if (event.getTenantId() == null) {
            log.warn("VoiceUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        VoiceUsageEvent.VoiceUsageData data = event.getVoiceUsageData();
        if (data == null) {
            log.warn("VoiceUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                event.getTenantId(), LocalDate.now(), "VOICE",
                event.getModelName());

        record.addVoiceUsage(
                data.getInputChars() != null ? data.getInputChars().longValue() : 0L,
                data.getOutputDuration() != null ? data.getOutputDuration().longValue() : 0L,
                data.getOutputSize() != null ? data.getOutputSize().longValue() : 0L);

        usageRecordRepository.save(record);
        log.debug("VOICE 使用量统计已更新，租户={}", event.getTenantId());
    }

    @Async
    @EventListener
    @Transactional
    public void onImageUsage(ImageUsageEvent event) {
        if (event.getTenantId() == null) {
            log.warn("ImageUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                event.getTenantId(), LocalDate.now(), "IMAGE",
                event.getModelName());

        record.addImageUsage(
                event.getInputChars() != null ? event.getInputChars().longValue() : 0L,
                event.getRequestCount() != null ? event.getRequestCount().longValue() : 0L,
                event.getSuccessCount() != null ? event.getSuccessCount().longValue() : 0L,
                event.getFailedCount() != null ? event.getFailedCount().longValue() : 0L);

        usageRecordRepository.save(record);
        log.debug("IMAGE 使用量统计已更新，租户={}", event.getTenantId());
    }

    @Async
    @EventListener
    @Transactional
    public void onMusicUsage(MusicUsageEvent event) {
        if (event.getTenantId() == null) {
            log.warn("MusicUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        MusicUsageEvent.MusicUsageData data = event.getMusicUsageData();
        if (data == null) {
            log.warn("MusicUsageEvent 数据为空，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                event.getTenantId(), LocalDate.now(), "MUSIC",
                event.getModelName());

        record.addMusicUsage(
                data.getInputChars() != null ? data.getInputChars().longValue() : 0L,
                data.getOutputDuration() != null ? data.getOutputDuration().longValue() : 0L,
                data.getOutputSize() != null ? data.getOutputSize().longValue() : 0L);

        usageRecordRepository.save(record);
        log.debug("MUSIC 使用量统计已更新，租户={}", event.getTenantId());
    }

    @Async
    @EventListener
    @Transactional
    public void onTokenUsage(TokenUsageEvent event) {
        if (event.getTenantId() == null) {
            log.warn("TokenUsageEvent 缺少租户信息，跳过统计");
            return;
        }

        UsageRecordEntity record = getOrCreateRecord(
                event.getTenantId(), LocalDate.now(), "CHAT",
                event.getModelName());

        record.addChatTokens(
                event.getPromptTokens() != null ? event.getPromptTokens().longValue() : 0L,
                event.getCompletionTokens() != null ? event.getCompletionTokens().longValue() : 0L);

        usageRecordRepository.save(record);
        log.debug("CHAT Token 统计已更新，租户={}", event.getTenantId());
    }

    private UsageRecordEntity getOrCreateRecord(String tenantId, LocalDate date, String type, String model) {
        return usageRecordRepository.findByTenantIdAndStatDateAndAiTypeAndModel(tenantId, date, type, model)
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setTenantId(tenantId);
                    newRecord.setStatDate(date);
                    newRecord.setAiType(type);
                    newRecord.setModel(model);
                    return newRecord;
                });
    }
}