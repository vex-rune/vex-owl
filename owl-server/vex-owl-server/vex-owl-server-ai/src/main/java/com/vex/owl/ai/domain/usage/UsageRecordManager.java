package com.vex.owl.ai.domain.usage;

import com.vex.event.Event;
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
 * <p>监听各种 AI 使用事件，聚合统计到数据库。</p>
 * <p>所有事件通过 {@link Event} 统一接收，从 EventMetadata 中提取 userId。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UsageRecordManager {

    private final UsageRecordRepository usageRecordRepository;

    @Async
    @EventListener
    @Transactional
    public void onEvent(Event event) {
        String userId = event.getMetadata().userId();
        if (userId == null || userId.isEmpty()) {
            log.warn("事件缺少 userId，跳过统计: {}", event.getMetadata().eventType());
            return;
        }

        Object payload = event.getPayload();

        if (payload instanceof VoiceUsageEvent voice) {
            onVoiceUsage(userId, voice);
        } else if (payload instanceof ImageUsageEvent image) {
            onImageUsage(userId, image);
        } else if (payload instanceof MusicUsageEvent music) {
            onMusicUsage(userId, music);
        } else if (payload instanceof TokenUsageEvent token) {
            onTokenUsage(userId, token);
        }
    }

    private void onVoiceUsage(String userId, VoiceUsageEvent event) {
        UsageRecordEntity record = usageRecordRepository
                .findByUserIdAndStatDateAndUsageType(userId, LocalDate.now(), "VOICE")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setUserId(userId);
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
        log.debug("VOICE 使用量统计已更新，userId={}", userId);
    }

    private void onImageUsage(String userId, ImageUsageEvent event) {
        UsageRecordEntity record = usageRecordRepository
                .findByUserIdAndStatDateAndUsageType(userId, LocalDate.now(), "IMAGE")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setUserId(userId);
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
        log.debug("IMAGE 使用量统计已更新，userId={}", userId);
    }

    private void onMusicUsage(String userId, MusicUsageEvent event) {
        UsageRecordEntity record = usageRecordRepository
                .findByUserIdAndStatDateAndUsageType(userId, LocalDate.now(), "MUSIC")
                .orElseGet(() -> {
                    UsageRecordEntity newRecord = new UsageRecordEntity();
                    newRecord.setUserId(userId);
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
        log.debug("MUSIC 使用量统计已更新，userId={}", userId);
    }

    private void onTokenUsage(String userId, TokenUsageEvent event) {
        UsageRecordEntity newRecord = new UsageRecordEntity();
        newRecord.setUserId(userId);
        newRecord.setStatDate(LocalDate.now());
        newRecord.setUsageType("CHAT");
        newRecord.setModelName(event.getModelName());

        newRecord.addChatUsage(
                event.getPromptTokens() != null ? event.getPromptTokens().longValue() : 0L,
                event.getCompletionTokens() != null ? event.getCompletionTokens().longValue() : 0L,
                event.getTotalTokens() != null ? event.getTotalTokens().longValue() : 0L
        );

        usageRecordRepository.save(newRecord);
        log.debug("CHAT Token 统计已更新，userId={}", userId);
    }

    // ==================== 查询 ====================

    public UsageStatResponse query(String userId, LocalDate startDate, LocalDate endDate) {
        UsageStatResponse.ChatUsage chat = sumChatUsage(userId, startDate, endDate);
        UsageStatResponse.VoiceUsage voice = sumVoiceUsage(userId, startDate, endDate);
        UsageStatResponse.ImageUsage image = sumImageUsage(userId, startDate, endDate);
        UsageStatResponse.MusicUsage music = sumMusicUsage(userId, startDate, endDate);

        long totalCallCount = nvl(chat.getCallCount()) + nvl(voice.getCallCount())
                + nvl(image.getRequestCount()) + nvl(music.getCallCount());

        return UsageStatResponse.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .chatUsage(chat)
                .voiceUsage(voice)
                .imageUsage(image)
                .musicUsage(music)
                .totalCallCount(totalCallCount)
                .build();
    }

    UsageStatResponse.ChatUsage sumChatUsage(String userId, LocalDate startDate, LocalDate endDate) {
        Object[] row = usageRecordRepository.sumChatUsageByUserIdAndDateRange(userId, startDate, endDate);
        return UsageStatResponse.ChatUsage.builder()
                .promptTokens(toLong(row[0]))
                .completionTokens(toLong(row[1]))
                .totalTokens(toLong(row[2]))
                .callCount(toLong(row[3]))
                .build();
    }

    UsageStatResponse.VoiceUsage sumVoiceUsage(String userId, LocalDate startDate, LocalDate endDate) {
        Object[] row = usageRecordRepository.sumVoiceUsageByUserIdAndDateRange(userId, startDate, endDate);
        return UsageStatResponse.VoiceUsage.builder()
                .callCount(toLong(row[0]))
                .inputChars(toLong(row[1]))
                .outputDuration(toLong(row[2]))
                .outputDurationSeconds(toLong(row[2]) != null ? toLong(row[2]) / 1000 : null)
                .outputSize(toLong(row[3]))
                .outputSizeMB(toLong(row[3]) != null ? toLong(row[3]) / 1024 / 1024 : null)
                .build();
    }

    UsageStatResponse.ImageUsage sumImageUsage(String userId, LocalDate startDate, LocalDate endDate) {
        Object[] row = usageRecordRepository.sumImageUsageByUserIdAndDateRange(userId, startDate, endDate);
        return UsageStatResponse.ImageUsage.builder()
                .requestCount(toLong(row[0]))
                .successCount(toLong(row[1]))
                .failedCount(toLong(row[2]))
                .inputChars(toLong(row[3]))
                .build();
    }

    UsageStatResponse.MusicUsage sumMusicUsage(String userId, LocalDate startDate, LocalDate endDate) {
        Object[] row = usageRecordRepository.sumMusicUsageByUserIdAndDateRange(userId, startDate, endDate);
        return UsageStatResponse.MusicUsage.builder()
                .callCount(toLong(row[0]))
                .inputChars(toLong(row[1]))
                .outputDuration(toLong(row[2]))
                .outputDurationSeconds(toLong(row[2]) != null ? toLong(row[2]) / 1000 : null)
                .outputSize(toLong(row[3]))
                .outputSizeMB(toLong(row[3]) != null ? toLong(row[3]) / 1024 / 1024 : null)
                .build();
    }

    private static Long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

    private static long nvl(Long value) {
        return value != null ? value : 0L;
    }
}
