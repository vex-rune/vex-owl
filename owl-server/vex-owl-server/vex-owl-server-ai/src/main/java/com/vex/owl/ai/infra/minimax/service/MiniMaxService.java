package com.vex.owl.ai.infra.minimax.service;

import com.vex.owl.ai.domain.event.AiContextMetadata;
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
 *
 * <p>整合 TTS 语音合成，文生图，音乐生成三大能力</p>
 *
 * <h2>主要功能</h2>
 * <ul>
 *   <li><b>TTS 语音合成</b>：将文本转换为自然流畅的语音</li>
 *   <li><b>文生图</b>：根据文本描述生成高质量图片</li>
 *   <li><b>音乐生成</b>：根据歌词和描述生成原创音乐</li>
 * </ul>
 *
 * <h2>使用前提</h2>
 * <ul>
 *   <li>需要配置有效的 MiniMax API Key</li>
 *   <li>API Key 需要在  中提供</li>
 *   <li>MiniMax 账户需要有足够的余额才能调用相应接口</li>
 * </ul>
 */
@Service
@Slf4j
public class MiniMaxService {

    private static final String PLATFORM = "minimax";

    private final MiniMaxClient minimaxClient;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数
     *
     * @param minimaxClient  MiniMax API 客户端，用于执行实际的 API 调用
     * @param eventPublisher Spring 事件发布器，用于发布使用统计事件
     */
    public MiniMaxService(MiniMaxClient minimaxClient, ApplicationEventPublisher eventPublisher) {
        this.minimaxClient = minimaxClient;
        this.eventPublisher = eventPublisher;
    }

    // ==================== TTS 语音合成 ====================

    /**
     * 将文本转换为语音（快捷方法）
     *
     * <p>使用默认配置将文本转换为语音，返回十六进制编码的音频数据</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>text</b>：要转换为语音的文本内容
     *       <ul>
     *         <li>长度限制：少于 10000 字符</li>
     *         <li>超过 3000 字符推荐使用流式输出</li>
     *         <li>支持特殊标记：换行符分段、&lt;#x#&gt; 控制停顿（x为秒数）</li>
     *       </ul>
     *   </li>
     *   <li><b>voiceId</b>：音色ID，决定生成的语音风格
     *       <ul>
     *         <li>示例值：male-qn-qingse（男性青年轻柔）、female-shaonv（女性少女）</li>
     *         <li>完整音色列表请参考 MiniMax 官方文档</li>
     *       </ul>
     *   </li>
     *   <li><b>format</b>：音频格式
     *       <ul>
     *         <li>可选值：mp3, wav, flac</li>
     *         <li>默认使用 mp3 格式</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置，包含 API Key 等认证信息
     *       <ul>
     *         <li>必须提供有效的 apiKey</li>
     *         <li>apiKey 不能为空或空白</li>
     *       </ul>
     *   </li>
     *   <li><b>context</b>：上下文信息，用于事件追踪（可选）
     *       <ul>
     *         <li>tenantId：租户ID</li>
     *         <li>sessionId：会话ID</li>
     *         <li>messageId：消息ID</li>
     *         <li>如果提供，会触发使用统计事件</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回十六进制编码的音频字节数组，可直接转换为音频文件</p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * byte[] audio = service.textToSpeech(
     *     "你好，欢迎使用语音合成服务",
     *     "female-shaonv",
     *     "mp3",
     *     modelProps,
     *     Map.of("tenantId", "tenant-123", "sessionId", "session-456")
     * );
     * Files.write(Path.of("output.mp3"), audio);
     * }</pre>
     *
     * @param text    要转换的文本内容
     * @param voiceId 音色ID
     * @param format  音频格式（mp3/wav/flac）
     * @param apiKey  apiKey
     * @param context 上下文信息（可选，用于事件追踪）
     * @return 十六进制编码的音频数据
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public byte[] textToSpeech(String text, String voiceId, String format,
                               String apiKey, Map<String, Object> context) {
        // 确保必填参数有默认值
        String audioFormat = (format == null || format.isBlank()) ? "mp3" : format;
        String voice = (voiceId == null || voiceId.isBlank()) ? "female-shaonv" : voiceId;

        // 如果 text 为空，使用默认文本
        String speechText = (text == null || text.isBlank()) ? "语音合成" : text;

        MiniMaxTtsRequest request = MiniMaxTtsRequest.builder()
                .model("speech-2.8-hd")
                .text(speechText)
                .stream(false)
                .outputFormat("hex")
                .voiceSetting(MiniMaxTtsRequest.VoiceSetting.builder()
                        .voiceId(voice).speed(1).vol(1).pitch(0).emotion("happy").build())
                .audioSetting(MiniMaxTtsRequest.AudioSetting.builder()
                        .sampleRate(32000).bitrate(128000).format(audioFormat).channel(1).build())
                .subtitleEnable(false)
                .build();

        return textToSpeech(request, apiKey, context);
    }

    /**
     * 将文本转换为语音（完整配置方法）
     *
     * <p>使用完整的请求配置将文本转换为语音，适合高级定制场景</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>request</b>：完整的 TTS 请求配置
     *       <ul>
     *         <li>model：模型版本，可选 speech-2.8-hd, speech-2.8-turbo 等</li>
     *         <li>text：要转换的文本内容</li>
     *         <li>stream：是否流式输出</li>
     *         <li>voiceSetting：音色设置（voiceId, speed, vol, pitch, emotion）</li>
     *         <li>audioSetting：音频设置（sampleRate, bitrate, format, channel）</li>
     *         <li>subtitleEnable：是否开启字幕</li>
     *         <li>languageBoost：语言增强</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置
     *       <ul>
     *         <li>必须提供有效的 apiKey</li>
     *       </ul>
     *   </li>
     *   <li><b>context</b>：上下文信息（可选）
     *       <ul>
     *         <li>如果提供，会触发 VoiceUsageEvent 事件</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * @param request 完整的 TTS 请求配置
     * @param apiKey  apiKey
     * @param context 上下文信息（可选）
     * @return 十六进制编码的音频数据
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public byte[] textToSpeech(MiniMaxTtsRequest request, String apiKey, Map<String, Object> context) {

        String authorization = "Bearer " + apiKey;

        // 校验必填参数
        validateTtsRequest(request);

        if(Boolean.FALSE.equals(request.getStream())){
            request.setStreamOptions(null);
        }

        MiniMaxTtsResponse response = minimaxClient.textToSpeech(authorization, request);


        if (!response.isSuccess()) {
            throw new MiniMaxException("TTS 失败: " + response);
        }

        String audioHex = response.getData().getAudio();
        if (context != null) {
            publishTtsUsageEvent(request, response, context);
        }
        return HexFormat.of().parseHex(audioHex);
    }

    /**
     * 将文本转换为语音（返回 URL）
     *
     * <p>使用默认配置将文本转换为语音，返回音频文件的下载地址（有效期 24 小时）</p>
     *
     * @param text    要转换的文本内容
     * @param voiceId 音色ID
     * @param format  音频格式（mp3/wav/flac）
     * @param apiKey  apiKey
     * @param context 上下文信息（可选）
     * @return 音频文件的 URL
     */
    public String textToSpeechUrl(String text, String voiceId, String format,
                                  String apiKey, Map<String, Object> context) {
        String audioFormat = (format == null || format.isBlank()) ? "mp3" : format;
        String voice = (voiceId == null || voiceId.isBlank()) ? "female-shaonv" : voiceId;
        String speechText = (text == null || text.isBlank()) ? "语音合成" : text;

        MiniMaxTtsRequest request = MiniMaxTtsRequest.builder()
                .model("speech-2.8-hd")
                .text(speechText)
                .stream(false)
                .outputFormat("url")
                .voiceSetting(MiniMaxTtsRequest.VoiceSetting.builder()
                        .voiceId(voice).speed(1).vol(1).pitch(0).emotion("happy").build())
                .audioSetting(MiniMaxTtsRequest.AudioSetting.builder()
                        .sampleRate(32000).bitrate(128000).format(audioFormat).channel(1).build())
                .subtitleEnable(false)
                .build();

        String authorization = "Bearer " + apiKey;
        validateTtsRequest(request);

        MiniMaxTtsResponse response = minimaxClient.textToSpeech(authorization, request);
        if (!response.isSuccess()) {
            throw new MiniMaxException("TTS 失败: " + response);
        }

        String audioUrl = response.getData().getAudio();
        if (context != null) {
            publishTtsUsageEvent(request, response, context);
        }
        return audioUrl;
    }

    // ==================== 文生图 ====================

    /**
     * 根据文本描述生成图片（快捷方法）
     *
     * <p>使用默认配置根据文本提示生成一张或多张图片</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>prompt</b>：图像的文本描述
     *       <ul>
     *         <li>最长 1500 字符</li>
     *         <li>描述越详细，生成效果越好</li>
     *         <li>支持中英文描述</li>
     *       </ul>
     *   </li>
     *   <li><b>model</b>：图像生成模型
     *       <ul>
     *         <li>可选值：image-01, image-01-live</li>
     *         <li>image-01：通用图像生成</li>
     *         <li>image-01-live：支持更多画风选项</li>
     *       </ul>
     *   </li>
     *   <li><b>aspectRatio</b>：图片宽高比
     *       <ul>
     *         <li>可选值：1:1（1024x1024）、16:9（1280x720）、4:3（1152x864）</li>
     *         <li>其他选项：3:2、2:3、3:4、9:16、21:9（仅 image-01）</li>
     *         <li>默认为 1:1</li>
     *       </ul>
     *   </li>
     *   <li><b>responseFormat</b>：返回格式
     *       <ul>
     *         <li>可选值：url（有效期24小时）、base64</li>
     *         <li>默认为 url</li>
     *       </ul>
     *   </li>
     *   <li><b>count</b>：生成图片数量
     *       <ul>
     *         <li>取值范围：1-9</li>
     *         <li>默认为 1</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置（必须包含有效的 apiKey）</li>
     *   <li><b>context</b>：上下文信息（可选）</li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回图片 URL 列表或 Base64 编码的图片列表</p>
     *
     * @param prompt         图片的文本描述（最长1500字符）
     * @param model          图像生成模型（image-01/image-01-live）
     * @param aspectRatio    宽高比（如 16:9, 1:1, 4:3）
     * @param responseFormat 返回格式（url/base64）
     * @param count          生成数量（1-9）
     * @param apiKey         apiKey
     * @param context        上下文信息（可选）
     * @return 图片URL列表或Base64列表
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public List<String> generateImage(String prompt, String model, String aspectRatio,
                                      String responseFormat, int count,
                                      String apiKey, Map<String, Object> context) {
        MiniMaxImageRequest request = MiniMaxImageRequest.builder()
                .model(model)
                .prompt(prompt)
                .aspectRatio(aspectRatio)
                .responseFormat(responseFormat)
                .n(count)
                .promptOptimizer(false)
                .aigcWatermark(false)
                .build();

        return generateImage(request, apiKey, context);
    }

    /**
     * 根据文本描述生成图片（完整配置方法）
     *
     * <p>使用完整的请求配置生成图片，适合高级定制场景</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>request</b>：完整的图像生成请求配置
     *       <ul>
     *         <li>model：图像生成模型</li>
     *         <li>prompt：图片描述</li>
     *         <li>aspectRatio：宽高比</li>
     *         <li>responseFormat：返回格式</li>
     *         <li>n：生成数量</li>
     *         <li>promptOptimizer：是否自动优化提示词</li>
     *         <li>seed：随机种子（相同种子产生相似图片）</li>
     *         <li>style：画风设置（仅 image-01-live）</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置</li>
     *   <li><b>context</b>：上下文信息（可选）</li>
     * </ul>
     *
     * @param request    完整的图像生成请求配置
     * @param apiKey apiKey
     * @param context    上下文信息（可选）
     * @return 图片URL列表或Base64列表
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public List<String> generateImage(MiniMaxImageRequest request,String  apiKey, Map<String, Object> context) {

        String authorization = "Bearer " + apiKey;
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

    /**
     * 生成音乐（快捷方法）
     *
     * <p>根据歌词和描述生成原创音乐，返回十六进制编码的音频数据</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>prompt</b>：音乐描述
     *       <ul>
     *         <li>用于指定风格、情绪和场景</li>
     *         <li>示例："流行音乐, 欢快, 适合派对"</li>
     *         <li>长度限制根据模型不同而不同</li>
     *       </ul>
     *   </li>
     *   <li><b>lyrics</b>：歌词
     *       <ul>
     *         <li>使用 \n 分隔每行</li>
     *         <li>支持结构标签：[Verse], [Chorus], [Bridge] 等</li>
     *         <li>如果 isInstrumental=true，则歌词可选</li>
     *       </ul>
     *   </li>
     *   <li><b>model</b>：音乐生成模型
     *       <ul>
     *         <li>music-2.6：推荐版本（付费用户）</li>
     *         <li>music-2.6-free：限免版本</li>
     *         <li>music-cover：翻唱版本</li>
     *       </ul>
     *   </li>
     *   <li><b>format</b>：音频格式
     *       <ul>
     *         <li>可选值：mp3, wav, flac</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置</li>
     *   <li><b>context</b>：上下文信息（可选）</li>
     * </ul>
     *
     * @param prompt     音乐描述（风格、情绪、场景）
     * @param lyrics     歌词（可选，支持结构标签）
     * @param model      音乐模型（music-2.6/music-2.6-free/music-cover）
     * @param format     音频格式（mp3/wav/flac）
     * @param apiKey apiKey
     * @param context    上下文信息（可选）
     * @return 十六进制编码的音频数据
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public byte[] generateMusic(String prompt, String lyrics, String model, String format,
                              String   apiKey, Map<String, Object> context) {
        MiniMaxMusicRequest request = MiniMaxMusicRequest.builder()
                .model(model)
                .prompt(prompt)
                .lyrics(lyrics)
                .isInstrumental(false)
                .outputFormat("hex")
                .audioSetting(MiniMaxMusicRequest.AudioSetting.builder()
                        .sampleRate(44100).bitrate(256000).format(format).build())
                .build();

        return generateMusic(request, apiKey, context);
    }

    /**
     * 生成音乐（完整配置方法）
     *
     * <p>使用完整的请求配置生成音乐，适合高级定制场景</p>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>request</b>：完整的音乐生成请求配置
     *       <ul>
     *         <li>model：音乐生成模型</li>
     *         <li>prompt：音乐描述</li>
     *         <li>lyrics：歌词</li>
     *         <li>isInstrumental：是否生成纯音乐</li>
     *         <li>outputFormat：输出格式（url/hex）</li>
     *         <li>audioSetting：音频设置</li>
     *         <li>stream：是否流式输出</li>
     *       </ul>
     *   </li>
     *   <li><b>modelProps</b>：模型配置</li>
     *   <li><b>context</b>：上下文信息（可选）</li>
     * </ul>
     *
     * @param request    完整的音乐生成请求配置
     * @param apiKey apiKey
     * @param context    上下文信息（可选）
     * @return 十六进制编码的音频数据
     * @throws MiniMaxException 如果 API Key 未配置或请求失败
     */
    public byte[] generateMusic(MiniMaxMusicRequest request,String apiKey, Map<String, Object> context) {

        String authorization = "Bearer " + apiKey;
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

    /**
     * 校验 TTS 请求参数
     * <p>确保必填参数有有效值，否则抛出明确的异常信息</p>
     *
     * @param request TTS 请求参数
     * @throws MiniMaxException 如果必填参数为空
     */
    private void validateTtsRequest(MiniMaxTtsRequest request) {
        if (request == null) {
            throw new MiniMaxException("TTS 请求参数不能为空");
        }
        if (request.getText() == null || request.getText().isBlank()) {
            throw new MiniMaxException("TTS 请求参数 text 不能为空");
        }
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw new MiniMaxException("TTS 请求参数 model 不能为空");
        }
        if (request.getVoiceSetting() == null) {
            throw new MiniMaxException("TTS 请求参数 voiceSetting 不能为空");
        }
        if (request.getVoiceSetting().getVoiceId() == null || request.getVoiceSetting().getVoiceId().isBlank()) {
            throw new MiniMaxException("TTS 请求参数 voiceSetting.voiceId 不能为空");
        }
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
