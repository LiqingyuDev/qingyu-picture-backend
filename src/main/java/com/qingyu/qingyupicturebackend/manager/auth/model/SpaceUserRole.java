package com.qingyu.qingyupicturebackend.manager.auth.model;

import lombok.Data;

import java.util.List;

/**
 * SpaceUserRole类继承自SpaceUserAuthConfig，用于定义空间用户的角色信息
 * 它提供了关于角色键、名称、描述以及权限的属性
 *
 * @author qingyu
 * @date 2025/2/19 11:52
 */
@Data
public class SpaceUserRole extends SpaceUserAuthConfig {

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

    /**
     * 角色所拥有的权限列表，表示该角色可以执行的操作
     */
    private List<String> permissions;


}
