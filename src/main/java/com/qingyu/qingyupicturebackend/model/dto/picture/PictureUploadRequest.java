package com.qingyu.qingyupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求体
 *
 * <p>该类用于封装图片上传请求中的参数。</p>
 *
 * @author liqingyu.dev@gmail.com
 * @since 2024/12/18
 */
@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 用于标识上传的图片的唯一ID。
     */
    private Long id;
    /**
     * 图片URL
     */
    private String fileUrl;

    /**
     * 空间ID
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
