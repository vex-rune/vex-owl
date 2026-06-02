package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MiniMaxMusicResponse {

    /**
     * 返回的生成数据
     */
    private MusicData data;

    /**
     * 本次会话的 ID
     * 用于在咨询/反馈时帮助定位问题
     */
    private String traceId;

    /**
     * 音频的附加信息
     */
    private ExtraInfo extraInfo;

    /**
     * 分析信息
     */
    private Object analysisInfo;

    /**
     * 本次请求的状态码和详情
     */
    @JsonProperty("base_resp")
    private BaseResp baseResp;

    /**
     * 生成的音频数据
     */
    @Data
    public static class MusicData {
        /**
         * 音频数据
         * 根据 output_format 参数返回：
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
        @JsonProperty("music_duration")
        private Integer musicDuration;

        /**
         * 音频采样率
         */
        @JsonProperty("music_sample_rate")
        private Integer musicSampleRate;

        /**
         * 音频声道数
         */
        @JsonProperty("music_channel")
        private Integer musicChannel;

        /**
         * 音频比特率
         */
        @JsonProperty("bitrate")
        private Integer bitrate;

        /**
         * 音频大小（字节）
         */
        @JsonProperty("music_size")
        private Integer musicSize;
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
