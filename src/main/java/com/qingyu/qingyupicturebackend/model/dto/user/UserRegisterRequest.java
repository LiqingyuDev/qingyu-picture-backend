package com.qingyu.qingyupicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 用户注册请求体
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/9 下午8:14
 */
@Data
public class UserRegisterRequest implements Serializable {
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 校验密码
     */
    private String checkPassword;
    private static final long serialVersionUID = 1L;

}
