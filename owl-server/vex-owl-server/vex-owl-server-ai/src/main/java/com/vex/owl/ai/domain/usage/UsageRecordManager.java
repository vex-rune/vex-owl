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

        UsageRecordEntity record = usageRecordRepository
                .findByTenantIdAndStatDateAndUsageType(event.getTenantId(), LocalDate.now(), "VOICE")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setTenantId(event.getTenantId());
                    newRecord.setStatDate(LocalDate.now());
                    newRecord.setUsageType("VOICE");
                    newRecord.setModelName(event.getModelName());
                    return newRecord;
                });

        record.addVoiceUsage(
                event.getInputChars() != null ? event.getInputChars().longValue() : 0L,
                event.getOutputDuration() != null ? event.getOutputDuration().longValue() : 0L,
                event.getOutputSize() != null ? event.getOutputSize().longValue() : 0L);

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

        UsageRecordEntity record = usageRecordRepository
                .findByTenantIdAndStatDateAndUsageType(event.getTenantId(), LocalDate.now(), "IMAGE")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setTenantId(event.getTenantId());
                    newRecord.setStatDate(LocalDate.now());
                    newRecord.setUsageType("IMAGE");
                    newRecord.setModelName(event.getModelName());
                    return newRecord;
                });

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

        UsageRecordEntity record = usageRecordRepository
                .findByTenantIdAndStatDateAndUsageType(event.getTenantId(), LocalDate.now(), "MUSIC")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setTenantId(event.getTenantId());
                    newRecord.setStatDate(LocalDate.now());
                    newRecord.setUsageType("MUSIC");
                    newRecord.setModelName(event.getModelName());
                    return newRecord;
                });

        record.addMusicUsage(
                event.getInputChars() != null ? event.getInputChars().longValue() : 0L,
                event.getOutputDuration() != null ? event.getOutputDuration().longValue() : 0L,
                event.getOutputSize() != null ? event.getOutputSize().longValue() : 0L);

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

        UsageRecordEntity record = usageRecordRepository
                .findByTenantIdAndStatDateAndUsageType(event.getTenantId(), LocalDate.now(), "CHAT")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setTenantId(event.getTenantId());
                    newRecord.setStatDate(LocalDate.now());
                    newRecord.setUsageType("CHAT");
                    newRecord.setModelName(event.getModelName());
                    return newRecord;
                });

        record.addChatUsage(
                event.getPromptTokens() != null ? event.getPromptTokens().longValue() : 0L,
                event.getCompletionTokens() != null ? event.getCompletionTokens().longValue() : 0L,
                event.getTotalTokens() != null ? event.getTotalTokens().longValue() : 0L
        );

        usageRecordRepository.save(record);
        log.debug("CHAT Token 统计已更新，租户={}", event.getTenantId());
    }
}
