package com.qingyu.qingyupicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serializable;

/**
 * @author qingyu
 * @description 添加空间内用户请求体
 * @date 2025/2/9 14:13
 */

@Data
public class SpaceUserEditRequest implements Serializable {
    /**
     * 用户空间关联主键
     */
    private Long id;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    private static final long serialVersionUID = 1L;
}
