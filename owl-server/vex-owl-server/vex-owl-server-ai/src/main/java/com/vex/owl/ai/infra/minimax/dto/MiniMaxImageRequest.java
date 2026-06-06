package com.vex.owl.ai.infra.minimax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiniMaxImageRequest {

    /**
     * 模型名称
     * 可选值：image-01, image-01-live
     */
    private String model;

    /**
     * 图像的文本描述
     * 最长 1500 字符
     */
    private String prompt;

    /**
     * 画风设置
     * 仅当 model 为 image-01-live 时生效
     */
    private Style style;

    /**
     * 图像宽高比，默认为 1:1
     * 可选值：1:1 (1024x1024), 16:9 (1280x720), 4:3 (1152x864),
     * 3:2 (1248x832), 2:3 (832x1248), 3:4 (864x1152),
     * 9:16 (720x1280), 21:9 (1344x576, 仅 image-01)
     */
    private String aspectRatio;

    /**
     * 生成图片的宽度（像素）
     * 仅当 model 为 image-01 时生效
     * 取值范围[512, 2048]，必须是 8 的倍数
     * 与 aspect_ratio 同时设置时，优先使用 width/height
     */
    private Integer width;

    /**
     * 生成图片的高度（像素）
     * 仅当 model 为 image-01 时生效
     * 取值范围[512, 2048]，必须是 8 的倍数
     * 与 aspect_ratio 同时设置时，优先使用 width/height
     */
    private Integer height;

    /**
     * 返回图片的形式，默认 url
     * 可选值：url（有效期 24 小时）, base64
     */
    private String responseFormat;

    /**
     * 随机种子
     * 使用相同的 seed 和参数，可以生成内容相近的图片
     * 如未提供，算法会对 n 张图单独生成随机种子
     */
    private Long seed;

    /**
     * 单次请求生成的图片数量
     * 取值范围[1, 9]，默认为 1
     */
    @Builder.Default
    private Integer n = 1;

    /**
     * 是否开启 prompt 自动优化，默认 false
     */
    @Builder.Default
    private Boolean promptOptimizer = false;

    /**
     * 是否在生成的图片中添加水印，默认 false
     */
    @Builder.Default
    private Boolean aigcWatermark = false;

    /**
     * 画风设置（仅 image-01-live 模型生效）
     */
    @Data
    @Builder
    public static class Style {
        /**
         * 画风类型
         * 可选值：photography（摄影）、illustration（插画）、
         * 3d_cg（3D建模）、anime（动漫）
         */
        private String type;

        /**
         * 照片风格
         * 可选值：studio（影楼风格）、authentic（真实场景）、
         * editorial（杂志封面）、film（胶片感）
         */
        private String photoStyle;

        /**
         * 插画风格
         * 可选值：flat（扁平插画）、semi-realistic（半写实）、
         * realistic（写实）、anime（动漫）、vector（矢量插画）、
         * pastel（马卡龙色）、ink（国潮）
         */
        private String illustrationStyle;
    }
}
