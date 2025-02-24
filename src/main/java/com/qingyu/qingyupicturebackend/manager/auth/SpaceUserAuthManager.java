package com.qingyu.qingyupicturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.qingyu.qingyupicturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.qingyu.qingyupicturebackend.manager.auth.model.SpaceUserRole;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.SpaceUser;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.SpaceRoleEnum;
import com.qingyu.qingyupicturebackend.model.enums.SpaceTypeEnum;
import com.qingyu.qingyupicturebackend.service.SpaceUserService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取 JSON 配置文件，并且根据角色获取权限列表的方法
 *
 * @author qingyu
 * @date 2025/2/19 12:11
 */
@Component
public class SpaceUserAuthManager {

    private static final SpaceUserAuthConfig jsonStrBean;

    @Resource
    private UserService userService;
    @Resource
    private SpaceUserService spaceUserService;

    static {
        // 读取 JSON 配置文件，获取权限列表
        String jsonStr = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        jsonStrBean = JSONUtil.toBean(jsonStr, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public static List<String> getSpaceUserAuthConfigByRole(String spaceRole) {
        if (spaceRole == null) {
            return null;
        }
        List<SpaceUserRole> roles = jsonStrBean.getRoles();
        for (SpaceUserRole role : roles) {
            if (role.getKey().equals(spaceRole)) {
                List<String> permissions = role.getPermissions();
                return permissions; // 找到匹配的角色后直接返回权限列表
            }
        }
        // 如果没有找到匹配的角色，返回空列表或null
        return null;
    }

    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getSpaceUserAuthConfigByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getSpaceUserAuthConfigByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }


}
