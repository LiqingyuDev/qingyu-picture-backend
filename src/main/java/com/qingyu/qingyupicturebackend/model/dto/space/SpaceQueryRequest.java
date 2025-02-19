package com.qingyu.qingyupicturebackend.model.dto.space;

import com.qingyu.qingyupicturebackend.model.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author qingyu
 * @description: 空间查询请求类(管理员)
 * @date 2025/1/4 20:27
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;
    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    private Integer spaceLevel;

    private static final long serialVersionUID = 1L;
}
