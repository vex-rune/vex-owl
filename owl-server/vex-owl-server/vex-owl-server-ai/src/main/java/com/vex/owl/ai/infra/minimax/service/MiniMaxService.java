package com.vex.owl.ai.infra.minimax.service;

import com.vex.owl.ai.domain.event.AiContextMetadata;
import com.vex.owl.ai.domain.event.ImageUsageEvent;
import com.vex.owl.ai.domain.event.MusicUsageEvent;
import com.vex.owl.ai.domain.event.VoiceUsageEvent;
import com.vex.owl.ai.domain.llm.repo.ModelProperties;
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
 *
 * <p>整合 TTS 语音合成，文生图，音乐生成三大能力</p>
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

    // ==================== TTS 语音合成 ====================

    public byte[] textToSpeech(String text, String voiceId, String format,
                               ModelProperties modelProps, Map<String, Object> context) {
        MiniMaxTtsRequest request = MiniMaxTtsRequest.builder()
                .model("speech-2.8-hd")
                .text(text)
                .stream(false)
                .outputFormat("hex")
                .voiceSetting(MiniMaxTtsRequest.VoiceSetting.builder()
                        .voiceId(voiceId).speed(1).vol(1).pitch(0).emotion("happy").build())
                .audioSetting(MiniMaxTtsRequest.AudioSetting.builder()
                        .sampleRate(32000).bitrate(128000).format(format).channel(1).build())
                .subtitleEnable(false)
                .build();

        return textToSpeech(request, modelProps, context);
    }

    public byte[] textToSpeech(MiniMaxTtsRequest request, ModelProperties modelProps, Map<String, Object> context) {
        validateModelProps(modelProps);
        String authorization = "Bearer " + modelProps.getApiKey();
        MiniMaxTtsResponse response = minimaxClient.textToSpeech(authorization, request);

        if (!response.isSuccess()) {
            throw new MiniMaxException("TTS 失败: " + response.getBaseResp().getStatusMsg());
        }

        String audioHex = response.getData().getAudio();
        if (context != null) {
            publishTtsUsageEvent(request, response, context);
        }
        return HexFormat.of().parseHex(audioHex);
    }

    // ==================== 文生图 ====================

    public List<String> generateImage(String prompt, String model, String aspectRatio,
                                       String responseFormat, int count,
                                       ModelProperties modelProps, Map<String, Object> context) {
        MiniMaxImageRequest request = MiniMaxImageRequest.builder()
                .model(model)
                .prompt(prompt)
                .aspectRatio(aspectRatio)
                .responseFormat(responseFormat)
                .n(count)
                .promptOptimizer(false)
                .aigcWatermark(false)
                .build();

        return generateImage(request, modelProps, context);
    }

    public List<String> generateImage(MiniMaxImageRequest request, ModelProperties modelProps, Map<String, Object> context) {
        validateModelProps(modelProps);
        String authorization = "Bearer " + modelProps.getApiKey();
        MiniMaxImageResponse response = minimaxClient.generateImage(authorization, request);

        if (!response.isSuccess()) {
            throw new MiniMaxException("图像生成失败: " + response.getBaseResp().getStatusMsg());
        }

        List<String> images = "base64".equalsIgnoreCase(request.getResponseFormat())
                ? response.getData().getImageBase64s()
                : response.getData().getImageUrls();

        if (images == null || images.isEmpty()) {
            throw new MiniMaxException("图像生成返回为空");
        }

        if (context != null) {
            publishImageUsageEvent(request, response, context);
        }
        return images;
    }

    // ==================== 音乐生成 ====================

    public byte[] generateMusic(String prompt, String lyrics, String model, String format,
                                 ModelProperties modelProps, Map<String, Object> context) {
        MiniMaxMusicRequest request = MiniMaxMusicRequest.builder()
                .model(model)
                .prompt(prompt)
                .lyrics(lyrics)
                .isInstrumental(false)
                .outputFormat("hex")
                .audioSetting(MiniMaxMusicRequest.AudioSetting.builder()
                        .sampleRate(44100).bitrate(256000).format(format).build())
                .build();

        return generateMusic(request, modelProps, context);
    }

    public byte[] generateMusic(MiniMaxMusicRequest request, ModelProperties modelProps, Map<String, Object> context) {
        validateModelProps(modelProps);
        String authorization = "Bearer " + modelProps.getApiKey();
        MiniMaxMusicResponse response = minimaxClient.generateMusic(authorization, request);

        if (!response.isSuccess()) {
            throw new MiniMaxException("音乐生成失败: " + response.getBaseResp().getStatusMsg());
        }

        String audioHex = response.getData().getAudio();
        if (context != null) {
            publishMusicUsageEvent(request, response, context);
        }
        return HexFormat.of().parseHex(audioHex);
    }

    // ==================== 私有方法 ====================

    private void validateModelProps(ModelProperties modelProps) {
        if (modelProps == null || modelProps.getApiKey() == null || modelProps.getApiKey().isBlank()) {
            throw new MiniMaxException("MiniMax API Key 未配置");
        }
    }

    private AiContextMetadata buildMetadata(String model, AiContextMetadata.AiType aiType, Map<String, Object> context) {
        AiContextMetadata metadata = AiContextMetadata.builder()
                .aiPlatform(PLATFORM)
                .aiModel(model)
                .aiType(aiType)
                .build();
        if (context != null) {
            metadata.setTenantId(getString(context, "tenantId"));
            metadata.setSessionId(getString(context, "sessionId"));
            metadata.setMessageId(getString(context, "messageId"));
        }
        return metadata;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private void publishTtsUsageEvent(MiniMaxTtsRequest request, MiniMaxTtsResponse response, Map<String, Object> context) {
        AiContextMetadata metadata = buildMetadata(request.getModel(), AiContextMetadata.AiType.VOICE, context);
        MiniMaxTtsResponse.ExtraInfo extra = response.getExtraInfo();

        VoiceUsageEvent event = new VoiceUsageEvent(metadata,
                VoiceUsageEvent.VoiceUsageData.builder()
                        .inputChars(request.getText() != null ? request.getText().length() : 0)
                        .outputDuration(extra != null ? extra.getAudioLength() : null)
                        .outputSize(extra != null ? extra.getAudioSize() : null)
                        .callCount(1)
                        .usageChars(extra != null ? extra.getUsageCharacters() : null)
                        .voiceId(request.getVoiceSetting() != null ? request.getVoiceSetting().getVoiceId() : null)
                        .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                        .build());

        log.info("VoiceUsageEvent: tenant={}, input={}chars, output={}ms, size={}bytes",
                metadata.getTenantId(),
                event.getData().getInputChars(),
                event.getData().getOutputDuration(),
                event.getData().getOutputSize());
        eventPublisher.publishEvent(event);
    }

    private void publishImageUsageEvent(MiniMaxImageRequest request, MiniMaxImageResponse response, Map<String, Object> context) {
        AiContextMetadata metadata = buildMetadata(request.getModel(), AiContextMetadata.AiType.IMAGE, context);
        MiniMaxImageResponse.Metadata imgMeta = response.getMetadata();

        ImageUsageEvent event = new ImageUsageEvent(metadata,
                ImageUsageEvent.ImageUsageData.builder()
                        .inputChars(request.getPrompt() != null ? request.getPrompt().length() : 0)
                        .requestCount(request.getN())
                        .successCount(imgMeta != null ? Integer.parseInt(imgMeta.getSuccessCount()) : null)
                        .failedCount(imgMeta != null ? Integer.parseInt(imgMeta.getFailedCount()) : null)
                        .aspectRatio(request.getAspectRatio())
                        .responseFormat(request.getResponseFormat())
                        .taskId(response.getId())
                        .build());

        log.info("ImageUsageEvent: tenant={}, input={}chars, success={}, failed={}",
                metadata.getTenantId(),
                event.getData().getInputChars(),
                event.getData().getSuccessCount(),
                event.getData().getFailedCount());
        eventPublisher.publishEvent(event);
    }

    private void publishMusicUsageEvent(MiniMaxMusicRequest request, MiniMaxMusicResponse response, Map<String, Object> context) {
        AiContextMetadata metadata = buildMetadata(request.getModel(), AiContextMetadata.AiType.MUSIC, context);
        MiniMaxMusicResponse.ExtraInfo extra = response.getExtraInfo();
        int inputChars = (request.getPrompt() != null ? request.getPrompt().length() : 0)
                + (request.getLyrics() != null ? request.getLyrics().length() : 0);

        MusicUsageEvent event = new MusicUsageEvent(metadata,
                MusicUsageEvent.MusicUsageData.builder()
                        .inputChars(inputChars)
                        .outputDuration(extra != null ? extra.getMusicDuration() : null)
                        .outputSize(extra != null ? extra.getMusicSize() : null)
                        .isInstrumental(request.getIsInstrumental())
                        .audioFormat(request.getAudioSetting() != null ? request.getAudioSetting().getFormat() : null)
                        .traceId(response.getTraceId())
                        .build());

        log.info("MusicUsageEvent: tenant={}, input={}chars, output={}ms, size={}bytes",
                metadata.getTenantId(),
                inputChars,
                event.getData().getOutputDuration(),
                event.getData().getOutputSize());
        eventPublisher.publishEvent(event);
    }

    public static class MiniMaxException extends RuntimeException {
        public MiniMaxException(String message) {
            super(message);
        }
    }
}
