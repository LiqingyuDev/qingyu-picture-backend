package com.qingyu.qingyupicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingyu
 * @description: 空间删除请求
 * @date 2025/1/5 11:41
 */
@Data
public class SpaceDeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 创建者id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
