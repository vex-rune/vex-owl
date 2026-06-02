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
