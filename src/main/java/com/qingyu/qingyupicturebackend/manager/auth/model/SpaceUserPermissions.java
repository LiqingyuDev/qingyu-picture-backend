package com.qingyu.qingyupicturebackend.manager.auth.model;

import lombok.Data;

/**
 * 空间用户权限
 *
 * @author qingyu
 * @date 2025/2/19 11:53
 */
@Data
public class SpaceUserPermissions extends SpaceUserAuthConfig {
    /**
     * 角色的唯一键，用于标识特定的角色
     */
    private String key;

    /**
     * 角色的名称，用于显示在用户界面中
     */
    private String name;

    /**
     * 角色的描述，提供关于角色职责或用途的详细信息
     */
    private String description;


}
