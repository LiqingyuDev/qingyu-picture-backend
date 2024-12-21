package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.user.UserQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.UserRoleEnum;
import com.qingyu.qingyupicturebackend.model.vo.LoginUserVO;
import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import com.qingyu.qingyupicturebackend.service.UserService;
import com.qingyu.qingyupicturebackend.mapper.UserMapper;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author qingyu
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-12-09 20:02:37
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    //region用户基础功能


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
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "qingyu";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()); // 使用 DigestUtils.md5Hex
    }

    /**
     * 获得脱敏后的登录用户信息
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
    //endregion


//region 用户管理:增删查改

    /**
     * 将 User 实体对象转换为 UserVO 对象。
     *
     * @param user 需要转换的 User 实体对象
     * @return 转换后的 UserVO 对象，如果传入的 User 对象为 null，则返回 null
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 将 User 实体对象列表转换为 UserVO 对象列表。
     *
     * @param userList 需要转换的 User 实体对象列表
     * @return 转换后的 UserVO 对象列表，如果传入的列表为空，则返回空列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 根据实体类获取查询条件。
     *
     * @param userQueryRequest 包含查询条件的请求对象
     * @return 构建好的 QueryWrapper 对象，用于后续的数据库查询
     * @throws BusinessException 如果请求参数为空，则抛出业务异常
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 创建一个 QueryWrapper 对象，用于构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 如果 id 不为空，则添加等于条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);

        // 如果 userRole 不为空且不为单个空白字符，则添加等于条件
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);

        // 如果 userAccount 不为空且不为单个空白字符，则添加模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);

        // 如果 userName 不为空且不为单个空白字符，则添加模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);

        // 如果 userProfile 不为空且不为单个空白字符，则添加模糊匹配条件
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);

        // 如果 sortOrder 为 "ascend"，则执行升序排序；否则执行降序排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    /**
     * 判断是否为管理员
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {

        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

//endregion


}
