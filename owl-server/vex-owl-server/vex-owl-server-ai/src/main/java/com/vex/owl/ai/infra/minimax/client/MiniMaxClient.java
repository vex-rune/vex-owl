package com.vex.owl.ai.infra.minimax.client;

import com.vex.owl.ai.infra.minimax.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * MiniMax 统一 API Client
 *
 * <p>整合 TTS 语音合成、文生图、音乐生成三大能力</p>
 *
 * <p>API 文档：</p>
 * <ul>
 *   <li>TTS: https://platform.minimaxi.com/docs/api-reference/speech-t2a-http</li>
 *   <li>图像生成: https://platform.minimaxi.com/docs/api-reference/image-generation-t2i</li>
 *   <li>音乐生成: https://platform.minimaxi.com/docs/api-reference/music-generation</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * @Service
 * public class MiniMaxService {
 *     private final MiniMaxClient client;
 *
 *     // TTS 语音合成
 *     public byte[] textToSpeech(String text) {
 *         MiniMaxTtsRequest request = MiniMaxTtsRequest.builder()
 *             .model("speech-2.8-hd")
 *             .text(text)
 *             .voiceSetting(VoiceSetting.builder()
 *                 .voiceId("male-qn-qingse")
 *                 .emotion("happy")
 *                 .build())
 *             .build();
 *         return client.textToSpeech(auth, request).getData().getAudio();
 *     }
 *
 *     // 文生图
 *     public List<String> generateImage(String prompt) {
 *         MiniMaxImageRequest request = MiniMaxImageRequest.builder()
 *             .model("image-01")
 *             .prompt(prompt)
 *             .aspectRatio("16:9")
 *             .n(3)
 *             .build();
 *         return client.generateImage(auth, request).getData().getImageUrls();
 *     }
 *
 *     // 音乐生成
 *     public byte[] generateMusic(String prompt, String lyrics) {
 *         MiniMaxMusicRequest request = MiniMaxMusicRequest.builder()
 *             .model("music-2.6")
 *             .prompt(prompt)
 *             .lyrics(lyrics)
 *             .build();
 *         return client.generateMusic(auth, request).getData().getAudio();
 *     }
 * }
 * }</pre>
 *
 * <h2>认证说明</h2>
 * <p>所有接口都需要在请求头中携带有效的 Authorization 信息：</p>
 * <ul>
 *   <li>格式：Bearer {API_KEY}</li>
 *   <li>API Key 需要在 MiniMax 平台获取</li>
 * </ul>
 */
@FeignClient(
    name = "minimax-client",
    url = "${minimax.api.url:https://api.minimaxi.com}"
)
public interface MiniMaxClient {

    // ==================== TTS 语音合成 ====================

    /**
     * 同步语音合成接口
     *
     * <p>使用 HTTP 协议进行同步语音合成，支持非流式和流式两种模式</p>
     *
     * <h3>接口信息</h3>
     * <ul>
     *   <li>接口地址：POST /v1/t2a_v2</li>
     *   <li>请求格式：JSON</li>
     *   <li>响应格式：JSON</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>authorization</b>：授权头（必填）
     *       <ul>
     *         <li>格式：Bearer {API_KEY}</li>
     *         <li>API Key 需要在 MiniMax 开放平台获取</li>
     *       </ul>
     *   </li>
     *   <li><b>request</b>：语音合成请求参数（必填）
     *       <ul>
     *         <li>model：模型版本（可选 speech-2.8-hd, speech-2.8-turbo 等）</li>
     *         <li>text：要转换的文本内容</li>
     *         <li>stream：是否流式输出</li>
     *         <li>voiceSetting：音色设置</li>
     *         <li>audioSetting：音频设置</li>
     *         <li>languageBoost：语言增强</li>
     *         <li>subtitleEnable：是否开启字幕</li>
     *         <li>outputFormat：输出格式（hex/url）</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回 MiniMaxTtsResponse 对象，包含：</p>
     * <ul>
     *   <li>baseResp：基础响应（状态码、状态信息）</li>
     *   <li>data：音频数据（audio 字段）</li>
     *   <li>extraInfo：额外信息（音频时长、大小、字符用量）</li>
     * </ul>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * MiniMaxTtsRequest request = MiniMaxTtsRequest.builder()
     *     .model("speech-2.8-hd")
     *     .text("你好，欢迎使用语音合成服务")
     *     .stream(false)
     *     .voiceSetting(MiniMaxTtsRequest.VoiceSetting.builder()
     *         .voiceId("female-shaonv")
     *         .speed(1)
     *         .emotion("happy")
     *         .build())
     *     .audioSetting(MiniMaxTtsRequest.AudioSetting.builder()
     *         .sampleRate(32000)
     *         .format("mp3")
     *         .build())
     *     .build();
     *
     * MiniMaxTtsResponse response = client.textToSpeech("Bearer " + apiKey, request);
     * if (response.isSuccess()) {
     *     String audioHex = response.getData().getAudio();
     *     // 处理音频数据...
     * }
     * }</pre>
     *
     * @param authorization 授权头，格式：Bearer {API_KEY}
     * @param request       语音合成请求参数
     * @return 语音合成响应
     * @see <a href="https://platform.minimaxi.com/docs/api-reference/speech-t2a-http">TTS API 文档</a>
     */
    @PostMapping("/v1/t2a_v2")
    MiniMaxTtsResponse textToSpeech(
        @RequestHeader("Authorization") String authorization,
        @RequestBody MiniMaxTtsRequest request
    );

    // ==================== 文生图 ====================

    /**
     * 文生图接口
     *
     * <p>使用文本描述生成图片</p>
     *
     * <h3>接口信息</h3>
     * <ul>
     *   <li>接口地址：POST /v1/image_generation</li>
     *   <li>请求格式：JSON</li>
     *   <li>响应格式：JSON</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>authorization</b>：授权头（必填）
     *       <ul>
     *         <li>格式：Bearer {API_KEY}</li>
     *       </ul>
     *   </li>
     *   <li><b>request</b>：图片生成请求参数（必填）
     *       <ul>
     *         <li>model：图像模型（image-01 或 image-01-live）</li>
     *         <li>prompt：图片描述（最长 1500 字符）</li>
     *         <li>aspectRatio：宽高比（1:1, 16:9, 4:3 等）</li>
     *         <li>responseFormat：返回格式（url 或 base64）</li>
     *         <li>n：生成数量（1-9）</li>
     *         <li>seed：随机种子</li>
     *         <li>promptOptimizer：是否自动优化提示词</li>
     *         <li>style：画风设置（仅 image-01-live）</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回 MiniMaxImageResponse 对象，包含：</p>
     * <ul>
     *   <li>baseResp：基础响应</li>
     *   <li>data：图片数据（imageUrls 或 imageBase64s）</li>
     *   <li>id：任务 ID</li>
     *   <li>metadata：元数据（成功数、失败数）</li>
     * </ul>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * MiniMaxImageRequest request = MiniMaxImageRequest.builder()
     *     .model("image-01")
     *     .prompt("一只可爱的橘猫在阳光下打盹")
     *     .aspectRatio("16:9")
     *     .n(3)
     *     .responseFormat("url")
     *     .build();
     *
     * MiniMaxImageResponse response = client.generateImage("Bearer " + apiKey, request);
     * if (response.isSuccess()) {
     *     List<String> imageUrls = response.getData().getImageUrls();
     *     // 处理图片 URL...
     * }
     * }</pre>
     *
     * @param authorization 授权头，格式：Bearer {API_KEY}
     * @param request        图片生成请求参数
     * @return 图片生成响应
     * @see <a href="https://platform.minimaxi.com/docs/api-reference/image-generation-t2i">图像生成 API 文档</a>
     */
    @PostMapping("/v1/image_generation")
    MiniMaxImageResponse generateImage(
        @RequestHeader("Authorization") String authorization,
        @RequestBody MiniMaxImageRequest request
    );

    // ==================== 音乐生成 ====================

    /**
     * 音乐生成接口
     *
     * <p>输入歌词和歌曲描述，进行歌曲生成</p>
     *
     * <h3>接口信息</h3>
     * <ul>
     *   <li>接口地址：POST /v1/music_generation</li>
     *   <li>请求格式：JSON</li>
     *   <li>响应格式：JSON</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>authorization</b>：授权头（必填）
     *       <ul>
     *         <li>格式：Bearer {API_KEY}</li>
     *       </ul>
     *   </li>
     *   <li><b>request</b>：音乐生成请求参数（必填）
     *       <ul>
     *         <li>model：音乐模型（music-2.6, music-cover 等）</li>
     *         <li>prompt：音乐描述（风格、情绪、场景）</li>
     *         <li>lyrics：歌词</li>
     *         <li>isInstrumental：是否生成纯音乐</li>
     *         <li>outputFormat：输出格式（url 或 hex）</li>
     *         <li>stream：是否流式输出</li>
     *         <li>audioSetting：音频设置</li>
     *         <li>audioUrl/audioBase64：参考音频（仅翻唱模型）</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回 MiniMaxMusicResponse 对象，包含：</p>
     * <ul>
     *   <li>baseResp：基础响应</li>
     *   <li>data：音频数据（audio 字段）</li>
     *   <li>traceId：追踪 ID</li>
     *   <li>extraInfo：额外信息（时长、大小）</li>
     * </ul>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * MiniMaxMusicRequest request = MiniMaxMusicRequest.builder()
     *     .model("music-2.6")
     *     .prompt("流行音乐, 欢快, 适合派对")
     *     .lyrics("[Verse]\\n阳光明媚\\n[Verse]\\n心情很好")
     *     .isInstrumental(false)
     *     .outputFormat("hex")
     *     .audioSetting(MiniMaxMusicRequest.AudioSetting.builder()
     *         .sampleRate(44100)
     *         .format("mp3")
     *         .build())
     *     .build();
     *
     * MiniMaxMusicResponse response = client.generateMusic("Bearer " + apiKey, request);
     * if (response.isSuccess()) {
     *     String audioHex = response.getData().getAudio();
     *     // 处理音频数据...
     * }
     * }</pre>
     *
     * @param authorization 授权头，格式：Bearer {API_KEY}
     * @param request        音乐生成请求参数
     * @return 音乐生成响应
     * @see <a href="https://platform.minimaxi.com/docs/api-reference/music-generation">音乐生成 API 文档</a>
     */
    @PostMapping("/v1/music_generation")
    MiniMaxMusicResponse generateMusic(
        @RequestHeader("Authorization") String authorization,
        @RequestBody MiniMaxMusicRequest request
    );
}
