package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiniMaxMusicRequest {

    /**
     * 使用的模型名称
     * 可选值：
     * - music-2.6（推荐）：文本生成音乐，仅限 Token Plan 用户和付费用户使用
     * - music-cover：基于参考音频生成翻唱版本
     * - music-2.6-free：music-2.6 的限免版本，所有用户可用
     * - music-cover-free：music-cover 的限免版本，所有用户可用
     */
    private String model;

    /**
     * 音乐的描述，用于指定风格、情绪和场景
     * 例如："流行音乐, 难过, 适合在下雨的晚上"
     *
     * 注意：
     * - music-2.6 / music-2.6-free 纯音乐（is_instrumental: true）：必填，长度限制 [1, 2000] 字符
     * - music-2.6 / music-2.6-free（非纯音乐）：可选，长度限制 [0, 2000] 字符
     * - music-cover / music-cover-free：必填，长度限制 [10, 300] 字符
     */
    private String prompt;

    /**
     * 歌曲歌词
     * 使用 \n 分隔每行
     *
     * 支持结构标签：
     * [Intro], [Verse], [Pre Chorus], [Chorus], [Interlude], [Bridge],
     * [Outro], [Post Chorus], [Transition], [Break], [Hook], [Build Up], [Inst], [Solo]
     *
     * 注意：
     * - music-2.6 / music-2.6-free 纯音乐（is_instrumental: true）：非必填
     * - music-2.6 / music-2.6-free（非纯音乐）：必填，长度限制 [1, 3500] 字符
     * - music-cover / music-cover-free：可选，如不传则通过 ASR 自动从参考音频中提取歌词
     */
    private String lyrics;

    /**
     * 是否使用流式传输，默认 false
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 音频的返回格式，默认 hex
     * 可选值：url（有效期 24 小时）, hex
     * 注意：stream=true 时，仅支持 hex 格式
     */
    @Builder.Default
    private String outputFormat = "hex";

    /**
     * 音频输出配置
     */
    private AudioSetting audioSetting;

    /**
     * 是否在音频末尾添加水印，默认 false
     * 仅在非流式（stream: false）请求时生效
     */
    @Builder.Default
    private Boolean aigcWatermark = false;

    /**
     * 是否根据 prompt 描述自动生成歌词
     * 仅 music-2.6 / music-2.6-free 支持
     * 设为 true 且 lyrics 为空时，系统会根据 prompt 自动生成歌词
     */
    @Builder.Default
    private Boolean lyricsOptimizer = false;

    /**
     * 是否生成纯音乐（无人声）
     * 仅 music-2.6 / music-2.6-free 支持
     * 设为 true 时，lyrics 字段非必填
     */
    @Builder.Default
    private Boolean isInstrumental = false;

    /**
     * 参考音频的 URL 地址
     * 仅用于 music-cover / music-cover-free 模型
     * audio_url 和 audio_base64 必须且只能提供其中一个
     * 与 cover_feature_id 互斥
     *
     * 参考音频要求：
     * - 时长：6 秒至 6 分钟
     * - 大小：最大 50 MB
     * - 格式：支持常见音频格式（mp3、wav、flac 等）
     */
    private String audioUrl;

    /**
     * Base64 编码的参考音频
     * 仅用于 music-cover / music-cover-free 模型
     * audio_url 和 audio_base64 必须且只能提供其中一个
     * 与 cover_feature_id 互斥
     *
     * 参考音频要求：
     * - 时长：6 秒至 6 分钟
     * - 大小：最大 50 MB
     * - 格式：支持常见音频格式（mp3、wav、flac 等）
     */
    private String audioBase64;

    /**
     * 翻唱前处理接口返回的特征 ID
     * 用于两步翻唱流程，支持修改歌词后生成翻唱
     * 仅用于 music-cover / music-cover-free 模型
     * 与 audio_url 和 audio_base64 互斥
     *
     * 注意：
     * - 传入时 lyrics 为必填（长度限制 [10, 1000] 字符）
     * - cover_feature_id 有效期为 24 小时
     * - 相同音频内容返回相同的 cover_feature_id
     */
    private String coverFeatureId;

    /**
     * 音频输出配置
     */
    @Data
    @Builder
    public static class AudioSetting {
        /**
         * 采样率
         */
        private Integer sampleRate;

        /**
         * 比特率
         */
        private Integer bitrate;

        /**
         * 音频格式
         * 可选值：mp3, wav, flac
         */
        private String format;
    }
}
