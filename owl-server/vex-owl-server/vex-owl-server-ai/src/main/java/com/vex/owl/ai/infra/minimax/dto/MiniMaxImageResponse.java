package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MiniMaxImageResponse {

    /**
     * 生成任务的 ID
     * 用于后续查询任务状态
     */
    private String id;

    /**
     * 返回的生成数据
     */
    private ImageData data;

    /**
     * 元数据信息
     */
    private Metadata metadata;

    /**
     * 本次请求的状态码和详情
     */
    @JsonProperty("base_resp")
    private BaseResp baseResp;

    /**
     * 生成的图片数据
     */
    @Data
    public static class ImageData {
        /**
         * 图片 URL 列表
         * url 格式时返回下载地址（有效期 24 小时）
         */
        private List<String> imageUrls;

        /**
         * 图片 Base64 列表
         * base64 格式时返回图片数据
         */
        private List<String> imageBase64s;
    }

    /**
     * 元数据信息
     */
    @Data
    public static class Metadata {
        /**
         * 失败的图片数量
         * 可能因内容安全检查未通过
         */
        @JsonProperty("failed_count")
        private String failedCount;

        /**
         * 成功的图片数量
         */
        @JsonProperty("success_count")
        private String successCount;
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
