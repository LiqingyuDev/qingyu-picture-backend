package com.qingyu.qingyupicturebackend.constant;

/**
 * 用户常量类
 *
 * 该接口定义了与用户相关的常量，包括用户登录状态、用户角色等。
 *
 * @author liqingyu.dev@gmail.com
 * @createTime 2024/12/9 下午9:07
 */
public interface UserConstant {

    /**
     * 用户登录状态的键名
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 管理员角色标识
     */
    String ADMIN_ROLE = "admin";

    /**
     * 默认用户角色标识
     */
    String DEFAULT_ROLE = "user";
}
