package com.qingyu.qingyupicturebackend.model.dto;

import lombok.Getter;

/**
 * @Description:
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/9 下午8:48
 */
@Getter
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}
