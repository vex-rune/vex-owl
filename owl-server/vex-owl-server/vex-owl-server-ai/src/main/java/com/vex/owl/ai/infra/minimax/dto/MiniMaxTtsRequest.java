package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiniMaxTtsRequest {

    /**
     * 模型版本
     * 可选范围：speech-2.8-hd, speech-2.8-turbo, speech-2.6-hd, speech-2.6-turbo,
     * speech-02-hd, speech-02-turbo, speech-01-hd, speech-01-turbo
     */
    private String model;

    /**
     * 需要合成语音的文本
     * 长度限制小于 10000 字符，若文本长度大于 3000 字符，推荐使用流式输出
     * 支持：
     * - 段落切换用换行符标记
     * - 停顿控制：使用 <#x#> 标记，x 为停顿时长（秒），范围 [0.01, 99.99]
     * - 发音替换：使用小括号包裹拼音或 IPA 音标
     * - 语气词标签（仅 speech-2.8-hd/turbo）：(laughs), (chuckle), (coughs), (sighs) 等
     */
    private String text;

    /**
     * 控制是否流式输出，默认 false
     */
    private Boolean stream;

    /**
     * 流式输出选项（stream=true 时有效）
     */
    private StreamOptions streamOptions;

    /**
     * 语音设置
     */
    private VoiceSetting voiceSetting;

    /**
     * 音频设置
     */
    private AudioSetting audioSetting;

    /**
     * 发音词典，用于指定特定文本的发音
     * 示例：{ "tone": ["处理/(chu3)(li3)", "危险/dangerous"] }
     */
    private PronunciationDict pronunciationDict;

    /**
     * 音色权重配置，用于混合多个音色
     */
    private TimbreWeights[] timbreWeights;

    /**
     * 小语种/方言增强识别能力
     * 可选值：Chinese, Chinese,Yue, English, Arabic, Russian, Spanish, French,
     * Portuguese, German, Turkish, Dutch, Ukrainian, Vietnamese, Indonesian,
     * Japanese, Italian, Korean, Thai, Polish, Romanian, Greek, Czech,
     * Finnish, Hindi, Bulgarian, Danish, Hebrew, Malay, Persian, Slovak,
     * Swedish, Croatian, Filipino, Hungarian, Norwegian, Slovenian, Catalan,
     * Nynorsk, Tamil, Afrikaans, auto
     */
    private String languageBoost;

    /**
     * 声音效果器设置
     * 支持格式：mp3, wav, flac（非流式）；mp3（流式）
     */
    private VoiceModify voiceModify;

    /**
     * 是否开启字幕服务，默认 false
     * 仅对 speech-2.8-hd/turbo, speech-2.6-hd/turbo, speech-02-hd/turbo,
     * speech-01-hd/turbo 模型有效
     */
    private Boolean subtitleEnable;

    /**
     * 字幕粒度，默认 sentence
     * - sentence：句级别时间戳
     * - word：词级别时间戳
     * - word_streaming：流式优化的词级别时间戳（仅 stream=true 时有效）
     */
    private String subtitleType;

    /**
     * 输出格式，默认 hex
     * - hex：返回 hex 编码的音频数据
     * - url：返回音频 URL（有效期 24 小时，仅非流式生效）
     */
    private String outputFormat;

    /**
     * 是否添加音频水印标识，默认 false
     * 仅对非流式合成生效
     */
    private Boolean aigcWatermark;

    @Data
    @Builder
    public static class VoiceSetting {
        /**
         * 语音 ID，用于指定音色
         */
        private String voiceId;

        /**
         * 语速，1 表示正常速度
         */
        private Integer speed;

        /**
         * 音量，1 表示正常音量
         */
        private Integer vol;

        /**
         * 音调偏移
         */
        private Integer pitch;

        /**
         * 情感标签，如 "happy", "sad", "angry" 等
         */
        private String emotion;
    }

    @Data
    @Builder
    public static class AudioSetting {
        /**
         * 采样率，默认 32000
         */
        private Integer sampleRate;

        /**
         * 比特率，默认 128000
         */
        private Integer bitrate;

        /**
         * 音频格式：mp3, wav, flac
         */
        private String format;

        /**
         * 声道数，1 表示单声道
         */
        private Integer channel;
    }

    @Data
    @Builder
    public static class StreamOptions {
        /**
         * 增量模式，true 时返回增量数据
         */
        private Boolean incremental;
    }

    @Data
    @Builder
    public static class PronunciationDict {
        /**
         * 发音规则列表
         * 格式：["文本/发音", "文本/发音"]
         */
        private String[] tone;
    }

    @Data
    @Builder
    public static class TimbreWeights {
        /**
         * 音色 ID
         */
        private String voiceId;

        /**
         * 权重值
         */
        private Double weight;
    }

    @Data
    @Builder
    public static class VoiceModify {
        /**
         * 变声类型
         */
        private String type;

        /**
         * 变声参数
         */
        private Double pitch;

        /**
         * 混响强度
         */
        private Double reverb;

        /**
         * 延迟效果
         */
        private Double delay;
    }
}
