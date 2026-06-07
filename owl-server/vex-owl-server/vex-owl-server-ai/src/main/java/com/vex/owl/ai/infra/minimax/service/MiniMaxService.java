package com.vex.owl.ai.infra.minimax.service;

import com.vex.owl.ai.domain.event.ImageUsageEvent;
import com.vex.owl.ai.domain.event.MusicUsageEvent;
import com.vex.owl.ai.domain.event.VoiceUsageEvent;
import com.vex.owl.ai.infra.minimax.client.MiniMaxClient;
import com.vex.owl.ai.infra.minimax.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * MiniMax 统一服务
 */
@Service
@Slf4j
public class MiniMaxService {

    private static final String PLATFORM = "minimax";

    private final MiniMaxClient minimaxClient;
    private final ApplicationEventPublisher eventPublisher;

    public MiniMaxService(MiniMaxClient minimaxClient, ApplicationEventPublisher eventPublisher) {
        this.minimaxClient = minimaxClient;
        this.eventPublisher = eventPublisher;
    }

    // ==================== 内部方法 ====================

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private void publishTtsUsageEvent(MiniMaxTtsRequest request, MiniMaxTtsResponse response, Map<String, Object> context) {
        String tenantId = getString(context, "tenantId");
        MiniMaxTtsResponse.ExtraInfo extra = response.getExtraInfo();

        VoiceUsageEvent event = VoiceUsageEvent.builder()
                .tenantId(tenantId)
                .sessionId(getString(context, "sessionId"))
                .modelName(request.getModel())
                .inputChars(request.getText() != null ? request.getText().length() : 0)
                .outputDuration(extra != null ? extra.getAudioLength() : null)
                .outputSize(extra != null ? extra.getAudioSize() : null)
                .callCount(1)
                .voiceId(request.getVoiceSetting() != null ? request.getVoiceSetting().getVoiceId() : null)
                .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                .build();

        log.info("VoiceUsageEvent: tenant={}, input={}chars, output={}ms", tenantId, event.getInputChars(), event.getOutputDuration());
        eventPublisher.publishEvent(event);
    }

    private void publishImageUsageEvent(MiniMaxImageRequest request, MiniMaxImageResponse response, Map<String, Object> context) {
        String tenantId = getString(context, "tenantId");
        MiniMaxImageResponse.Metadata imgMeta = response.getMetadata();

        ImageUsageEvent event = ImageUsageEvent.builder()
                .tenantId(tenantId)
                .sessionId(getString(context, "sessionId"))
                .modelName(request.getModel())
                .inputChars(request.getPrompt() != null ? request.getPrompt().length() : 0)
                .requestCount(request.getN())
                .successCount(imgMeta != null ? Integer.parseInt(imgMeta.getSuccessCount()) : null)
                .failedCount(imgMeta != null ? Integer.parseInt(imgMeta.getFailedCount()) : null)
                .aspectRatio(request.getAspectRatio())
                .build();

        log.info("ImageUsageEvent: tenant={}, input={}chars, success={}, failed={}",
                tenantId, event.getInputChars(), event.getSuccessCount(), event.getFailedCount());
        eventPublisher.publishEvent(event);
    }

    private void publishMusicUsageEvent(MiniMaxMusicRequest request, MiniMaxMusicResponse response, Map<String, Object> context) {
        String tenantId = getString(context, "tenantId");
        MiniMaxMusicResponse.ExtraInfo extra = response.getExtraInfo();
        int inputChars = (request.getPrompt() != null ? request.getPrompt().length() : 0)
                + (request.getLyrics() != null ? request.getLyrics().length() : 0);

        MusicUsageEvent event = MusicUsageEvent.builder()
                .tenantId(tenantId)
                .sessionId(getString(context, "sessionId"))
                .modelName(request.getModel())
                .inputChars(inputChars)
                .outputDuration(extra != null ? extra.getMusicDuration() : null)
                .outputSize(extra != null ? extra.getMusicSize() : null)
                .isInstrumental(request.getIsInstrumental())
                .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                .traceId(response.getTraceId())
                .build();

        log.info("MusicUsageEvent: tenant={}, input={}chars, output={}ms, size={}bytes",
                tenantId, inputChars, event.getOutputDuration(), event.getOutputSize());
        eventPublisher.publishEvent(event);
    }

    public static class MiniMaxException extends RuntimeException {
        public MiniMaxException(String message) {
            super(message);
        }
    }
}