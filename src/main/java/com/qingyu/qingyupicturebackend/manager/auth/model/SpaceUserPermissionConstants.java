package com.qingyu.qingyupicturebackend.manager.auth.model;

/**
 * 定义空间用户的权限常量。
 *
 * @author qingyu
 * @date 2025/2/19 12:06
 */
public interface SpaceUserPermissionConstants {

    /**
     * 空间用户管理权限，允许用户管理空间成员，包括添加或移除成员。
     */
    String SPACE_USER_MANAGE = "spaceUser:manage"; // 成员管理

    /**
     * 查看图片权限，允许用户查看空间中的图片。
     */
    String PICTURE_VIEW = "picture:view"; // 查看图片

    /**
     * 上传图片权限，允许用户上传图片到空间中。
     */
    String PICTURE_UPLOAD = "picture:upload"; // 上传图片

    /**
     * 修改图片权限，允许用户编辑已上传的图片信息。
     */
    String PICTURE_EDIT = "picture:edit"; // 修改图片

    /**
     * 删除图片权限，允许用户删除空间中的图片。
     */
    String PICTURE_DELETE = "picture:delete"; // 删除图片
}
