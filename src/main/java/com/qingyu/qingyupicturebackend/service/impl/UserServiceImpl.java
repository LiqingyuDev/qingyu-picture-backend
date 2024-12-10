package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.UserLoginRequest;
import com.qingyu.qingyupicturebackend.model.dto.UserRegisterRequest;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.UserRoleEnum;
import com.qingyu.qingyupicturebackend.model.vo.LoginUserVO;
import com.qingyu.qingyupicturebackend.service.UserService;
import com.qingyu.qingyupicturebackend.mapper.UserMapper;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用户服务实现类
 * @author qingyu
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-09 20:02:37
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 校验参数
        // 检查账号是否为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");
        // 检查账号长度是否合法
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        // 检查密码长度是否合法
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8, ErrorCode.PARAMS_ERROR, "用户密码过短");
        // 检查两次输入的密码是否一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 检查账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 加密密码
        String encryptedPassword = getEncryptPassword(userPassword);

        // 写入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword); // 使用加密后的密码
        user.setUserName("默认昵称");
        user.setUserRole(UserRoleEnum.USER.getValue());

        boolean saveResult = this.save(user);
        if (saveResult) {
            return user.getId(); // 返回新注册用户的ID
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验参数
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 2. 对用户传递的密码进行加密
        String encryptedPassword = getEncryptPassword(userPassword);

        // 3. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);

        // 4. 验证密码
        if (user == null || !user.getUserPassword().equals(encryptedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 5. 保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        // 6. 返回登录信息
        return getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否已经登录
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        // 根据用户ID查询用户信息
        Long id = currentUser.getId();
        currentUser = this.getById(id);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        return currentUser;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        // 判断是否已经登录
        HttpSession session = request.getSession();
        Object loginState = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (loginState == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        // 清除session中的登录状态
        session.removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    private String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "qingyu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()); // 使用 DigestUtils.md5Hex
    }

    /**
     * 获得脱敏后的用户信息
     *
     * @param user 包含用户详细信息的 User 对象
     * @return 包含脱敏后用户信息的 LoginUserVO 对象
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }
}
