package com.qingyu.qingyupicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 用户添加请求体
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/10 下午8:42
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
