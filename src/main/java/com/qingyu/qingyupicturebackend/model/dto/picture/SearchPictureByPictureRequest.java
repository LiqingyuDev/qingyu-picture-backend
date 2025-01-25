package com.qingyu.qingyupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingyu
 * @description 以图搜图请求
 * @date 2025/1/24 14:09
 */

@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
