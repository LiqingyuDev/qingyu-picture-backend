package com.qingyu.qingyupicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.LoginUserVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qingyu
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2024-12-09 20:02:37
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 注册成功的用户ID
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request      HTTP请求对象
     * @return 登录成功的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request HTTP请求对象
     * @return 当前登录的用户信息
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 获得脱敏后的用户信息
     *
     * @param user 包含用户详细信息的 User 对象
     * @return 包含脱敏后用户信息的 LoginUserVO 对象
     */
    LoginUserVO getLoginUserVO(User user);
    /**
     * 用户注销
     *
     * @param request HTTP请求对象
     * @return 注销是否成功
     */
    Boolean userLogout(HttpServletRequest request);
}
