package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MiniMaxTtsResponse {

    /**
     * 返回的合成数据对象
     * 可能为 null，需进行非空判断
     */
    private TtsData data;

    /**
     * 本次会话的 ID，用于在咨询/反馈时帮助定位问题
     */
    private String traceId;

    /**
     * 音频的附加信息
     */
    private ExtraInfo extraInfo;

    /**
     * 本次请求的状态码和详情
     */
    @JsonProperty("base_resp")
    private BaseResp baseResp;

    /**
     * 合成数据对象
     */
    @Data
    public static class TtsData {
        /**
         * 音频数据，根据 output_format 参数返回：
         * - hex 格式：hex 编码的音频字符串
         * - url 格式：音频下载地址（有效期 24 小时）
         */
        private String audio;

        /**
         * 音频状态
         * 0: 处理中
         * 1: 处理中
         * 2: 完成
         */
        private Integer status;
    }

    /**
     * 音频附加信息
     */
    @Data
    public static class ExtraInfo {
        /**
         * 音频时长（毫秒）
         */
        @JsonProperty("audio_length")
        private Integer audioLength;

        /**
         * 音频采样率
         */
        @JsonProperty("audio_sample_rate")
        private Integer audioSampleRate;

        /**
         * 音频大小（字节）
         */
        @JsonProperty("audio_size")
        private Integer audioSize;

        /**
         * 音频比特率
         */
        @JsonProperty("bitrate")
        private Integer bitrate;

        /**
         * 文本词数
         */
        @JsonProperty("word_count")
        private Integer wordCount;

        /**
         * 不可见字符占比
         */
        @JsonProperty("invisible_character_ratio")
        private Double invisibleCharacterRatio;

        /**
         * 实际消耗的字符数
         */
        @JsonProperty("usage_characters")
        private Integer usageCharacters;

        /**
         * 音频格式
         */
        @JsonProperty("audio_format")
        private String audioFormat;

        /**
         * 音频声道数
         */
        @JsonProperty("audio_channel")
        private Integer audioChannel;

        /**
         * 字幕数据（当 subtitle_enable=true 时返回）
         * 格式：根据 subtitle_type 参数返回句级别或词级别时间戳
         */
        private Object subtitle;
    }

    /**
     * 基础响应状态
     */
    @Data
    public static class BaseResp {
        /**
         * 状态码
         * 0: 成功
         * 其他值: 失败，详情见 status_msg
         */
        @JsonProperty("status_code")
        private Integer statusCode;

        /**
         * 状态详情
         */
        @JsonProperty("status_msg")
        private String statusMsg;
    }

    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return baseResp != null && baseResp.getStatusCode() != null
               && baseResp.getStatusCode() == 0;
    }
}
