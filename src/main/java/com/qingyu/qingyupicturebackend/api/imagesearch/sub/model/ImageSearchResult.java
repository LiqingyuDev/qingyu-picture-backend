package com.qingyu.qingyupicturebackend.api.imagesearch.sub.model;

import lombok.Data;

/**
 *
 * @description 接受 API 的返回值
 * @author qingyu
 * @date 2025/1/21 17:03
 */

@Data
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    private String thumbUrl;

    /**
     * 来源地址
     */
    private String fromUrl;
}
