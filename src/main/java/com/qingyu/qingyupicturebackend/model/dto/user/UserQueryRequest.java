package com.qingyu.qingyupicturebackend.model.dto.user;

import com.qingyu.qingyupicturebackend.model.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description: 用户查询请求，需要继承公共包中的 PageRequest 来支持分页查询
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/10 下午8:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}

