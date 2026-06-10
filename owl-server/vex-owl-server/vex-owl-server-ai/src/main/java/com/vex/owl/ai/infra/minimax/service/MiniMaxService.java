package com.vex.owl.ai.infra.minimax.service;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.event.ImageUsageEvent;
import com.vex.owl.ai.domain.event.MusicUsageEvent;
import com.vex.owl.ai.domain.event.VoiceUsageEvent;
import com.vex.owl.ai.infra.minimax.client.MiniMaxClient;
import com.vex.owl.ai.infra.minimax.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MiniMax 统一服务
 */
@Service
@Slf4j
public class MiniMaxService {

    private static final String PLATFORM = "minimax";

    private final MiniMaxClient minimaxClient;
    private final EventPublisher eventPublisher;

    public MiniMaxService(MiniMaxClient minimaxClient, EventPublisher eventPublisher) {
        this.minimaxClient = minimaxClient;
        this.eventPublisher = eventPublisher;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private void publishTtsUsageEvent(MiniMaxTtsRequest request, MiniMaxTtsResponse response, Map<String, Object> context) {
        MiniMaxTtsResponse.ExtraInfo extra = response.getExtraInfo();

        VoiceUsageEvent event = VoiceUsageEvent.builder()
                .userId(getString(context, "userId"))
                .sessionId(getString(context, "sessionId"))
                .provider(PLATFORM)
                .modelName(request.getModel())
                .inputChars(request.getText() != null ? request.getText().length() : 0)
                .outputDuration(extra != null ? extra.getAudioLength() : null)
                .outputSize(extra != null ? extra.getAudioSize() : null)
                .callCount(1)
                .voiceId(request.getVoiceSetting() != null ? request.getVoiceSetting().getVoiceId() : null)
                .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                .build();

        log.debug("VoiceUsageEvent: input={}chars, output={}ms", event.getInputChars(), event.getOutputDuration());
        eventPublisher.publish("VoiceUsageEvent", event);
    }

    private void publishImageUsageEvent(MiniMaxImageRequest request, MiniMaxImageResponse response, Map<String, Object> context) {
        MiniMaxImageResponse.Metadata imgMeta = response.getMetadata();

        ImageUsageEvent event = ImageUsageEvent.builder()
                .userId(getString(context, "userId"))
                .sessionId(getString(context, "sessionId"))
                .provider(PLATFORM)
                .modelName(request.getModel())
                .inputChars(request.getPrompt() != null ? request.getPrompt().length() : 0)
                .requestCount(request.getN())
                .successCount(imgMeta != null ? Integer.parseInt(imgMeta.getSuccessCount()) : null)
                .failedCount(imgMeta != null ? Integer.parseInt(imgMeta.getFailedCount()) : null)
                .aspectRatio(request.getAspectRatio())
                .build();

        log.debug("ImageUsageEvent: input={}chars, success={}, failed={}",
                event.getInputChars(), event.getSuccessCount(), event.getFailedCount());
        eventPublisher.publish("ImageUsageEvent", event);
    }

    private void publishMusicUsageEvent(MiniMaxMusicRequest request, MiniMaxMusicResponse response, Map<String, Object> context) {
        MiniMaxMusicResponse.ExtraInfo extra = response.getExtraInfo();
        int inputChars = (request.getPrompt() != null ? request.getPrompt().length() : 0)
                + (request.getLyrics() != null ? request.getLyrics().length() : 0);

        MusicUsageEvent event = MusicUsageEvent.builder()
                .userId(getString(context, "userId"))
                .sessionId(getString(context, "sessionId"))
                .provider(PLATFORM)
                .modelName(request.getModel())
                .inputChars(inputChars)
                .outputDuration(extra != null ? extra.getMusicDuration() : null)
                .outputSize(extra != null ? extra.getMusicSize() : null)
                .isInstrumental(request.getIsInstrumental())
                .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                .build();

        log.debug("MusicUsageEvent: input={}chars, output={}ms, size={}bytes",
                inputChars, event.getOutputDuration(), event.getOutputSize());
        eventPublisher.publish("MusicUsageEvent", event);
    }

    public static class MiniMaxException extends RuntimeException {
        public MiniMaxException(String message) {
            super(message);
        }
    }
}