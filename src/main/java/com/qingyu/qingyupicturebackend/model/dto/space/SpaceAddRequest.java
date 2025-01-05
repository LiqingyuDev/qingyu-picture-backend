package com.qingyu.qingyupicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingyu
 * @description: 空间添加请求类
 * @date 2025/1/4 20:06
 */
@Data
public class SpaceAddRequest implements Serializable {
    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间图片的最大总大小
     */
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    private Long maxCount;

    private static final long serialVersionUID = 1L;
}
