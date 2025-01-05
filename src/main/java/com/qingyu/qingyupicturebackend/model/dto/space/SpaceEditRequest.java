package com.qingyu.qingyupicturebackend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingyu
 * @description: 空间编辑请求类(用户)
 * @date 2025/1/4 20:26
 */

@Data
public class SpaceEditRequest implements Serializable {

    /**
     * 空间 id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    private static final long serialVersionUID = 1L;
}
