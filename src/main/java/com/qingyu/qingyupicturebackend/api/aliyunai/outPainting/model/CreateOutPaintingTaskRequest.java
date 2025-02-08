package com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求体类，用于封装发送给阿里云AI的图像画面扩展请求
 * <a href="https://help.aliyun.com/zh/model-studio/developer-reference/image-scaling-api?spm=a2c4g.11186623.0.0.4216408dC3TvF3">...</a>
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    /**
     * 模型名称。
     */
    private String model = "image-out-painting";

    /**
     * 输入信息，包含需要转换或处理的图片URL。
     */
    private Input input;

    /**
     * 参数信息，包含可选参数设置。
     */
    private Parameters parameters;

    /**
     * 输入类，包含图片的URL。
     */
    @Data
    public static class Input {
        /**
         * 图片的URL。
         */
        @Alias("image_url")
        private String imageUrl;
    }

    /**
     * 参数类，包含可选参数设置。
     */
    @Data
    public static class Parameters implements  Serializable {
        /**
         * 旋转角度。
         */

        private Integer angle;

        /**
         * 输出比例。
         */
        @Alias("output_ratio")
        private String outputRatio;

        /**
         * X轴缩放比例。
         */
        @Alias("x_scale")
        @JsonProperty("xScale")
        private Float xScale;

        /**
         * Y轴缩放比例。
         */
        @Alias("y_scale")
        @JsonProperty("yScale")
        private Float yScale;

        /**
         * 上边距偏移量。
         */
        @Alias("top_offset")
        private Integer topOffset;

        /**
         * 下边距偏移量。
         */
        @Alias("bottom_offset")
        private Integer bottomOffset;

        /**
         * 左边距偏移量。
         */
        @Alias("left_offset")
        private Integer leftOffset;

        /**
         * 右边距偏移量。
         */
        @Alias("right_offset")
        private Integer rightOffset;

        /**
         * 是否启用最佳质量。
         */
        @Alias("best_quality")
        private Boolean bestQuality;

        /**
         * 是否限制图片大小。
         */
        @Alias("limit_image_size")
        private Boolean limitImageSize;

        /**
         * 是否添加水印。
         */
        @Alias("add_watermark")
        private Boolean addWatermark=false;
    }
}
