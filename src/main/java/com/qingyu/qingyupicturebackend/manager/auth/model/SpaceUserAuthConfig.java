package com.qingyu.qingyupicturebackend.manager.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 团队空间权限配置对应类
 * 该类用于定义团队空间中用户权限和角色的配置
 *
 * @author qingyu
 * @date 2025/2/19 11:48
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAuthConfig {
    /**
     * 权限列表
     * 存储用户在团队空间中的具体权限，如读取、写入等
     */
    private List<String> permissions;

    /**
     * 角色列表
     * 存储用户在团队空间中扮演的角色，如管理员、成员等
     */
    private List<SpaceUserRole> roles;

}
