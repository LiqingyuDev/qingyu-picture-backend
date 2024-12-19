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
     * 图片ID
     *
     * <p>用于标识上传的图片的唯一ID。</p>
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
