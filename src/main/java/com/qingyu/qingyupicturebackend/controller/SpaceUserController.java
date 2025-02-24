package com.qingyu.qingyupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.qingyu.qingyupicturebackend.manager.auth.model.SpaceUserPermissionConstants;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.SpaceUser;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.request.DeleteRequest;
import com.qingyu.qingyupicturebackend.model.vo.SpaceUserVO;
import com.qingyu.qingyupicturebackend.service.UserService;
import com.qingyu.qingyupicturebackend.service.impl.SpaceUserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author qingyu
 * @description 空间用户关联控制
 * @date 2025/2/10 12:15
 */
@RestController
@RequestMapping("/spaceUser")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserServiceImpl spaceUserService;
    @Resource
    private UserService userService;

    /**
     * 在指定空间内添加新用户
     *
     * @param spaceUserAddRequest 包含要添加的用户信息和空间信息的请求对象
     * @return 数据库返回的主键ID
     */
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstants.SPACE_USER_MANAGE)
    @PostMapping("/add")
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Long spaceUserId = spaceUserService.addSpaceUser(spaceUserAddRequest);
        //返回主键ID
        return ResultUtils.success(spaceUserId);
    }

    /**
     * 从空间移除成员
     *
     * @param request HTTP 请求对象，用于获取上下文信息（如当前用户信息）
     * @return 成功或失败信息
     */
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstants.SPACE_USER_MANAGE)
    @PostMapping("/delete/{spaceUserId}")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long spaceUserId = deleteRequest.getId();
        if (spaceUserId == null || spaceUserId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = spaceUserService.removeById(spaceUserId);
        return ResultUtils.success(result);
    }

    /**
     * 查询空间用户关联，并将结果转换为视图对象列表
     *
     * @param spaceUserQueryRequest 查询请求参数，包含空间信息和用户信息等查询条件
     * @param request               HTTP 请求对象，用于获取上下文信息（如当前用户信息）
     * @return 转换后的空间用户关联视图对象列表
     */
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstants.SPACE_USER_MANAGE)
    @GetMapping("/list")
    public BaseResponse<List<SpaceUserVO>> listSpaceUsers(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 创建查询条件
        QueryWrapper<SpaceUser> queryWrapper = spaceUserService.getQueryWrapper(spaceUserQueryRequest);
        // 执行查询
        List<SpaceUser> spaceUserList = spaceUserService.list(queryWrapper);
        // 封装为 VO
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtils.success(spaceUserVOList);
    }

    /**
     * 查询某个空间用户关联的视图对象
     *
     * @param spaceUserQueryRequest
     * @param request
     * @return
     */
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstants.SPACE_USER_MANAGE)
    @PostMapping("/get")
    public BaseResponse<SpaceUserVO> getSpaceUserById(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long userId = spaceUserQueryRequest.getUserId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        ThrowUtils.throwIf(userId == null || spaceId == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        SpaceUser spaceUser = spaceUserService.getOne(Wrappers.lambdaQuery(SpaceUser.class)
                .eq(SpaceUser::getUserId, userId)
                .eq(SpaceUser::getSpaceId, spaceId));

        SpaceUserVO spaceUserVO = spaceUserService.getSpaceUserVO(spaceUser);
        return ResultUtils.success(spaceUserVO);
    }

    /**
     * 团队空间所有者控制成员权限
     *
     * @param spaceUserEditRequest
     * @param request
     * @return
     */
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstants.SPACE_USER_MANAGE)
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);

        //数据校验
        spaceUserService.validSpaceUser(spaceUser, false);

        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 查询我加入的空间(用户空间多对多)
     *
     * @param request
     * @return
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        List<SpaceUser> spaceUserList = spaceUserService.list(Wrappers
                .lambdaQuery(SpaceUser.class)
                .eq(SpaceUser::getUserId, loginUser.getId()));

        ThrowUtils.throwIf(CollUtil.isEmpty(spaceUserList), ErrorCode.NOT_FOUND_ERROR);
        List<SpaceUserVO> spaceUserVOList = spaceUserService.getSpaceUserVOList(spaceUserList);
        return ResultUtils.success(spaceUserVOList);


    }
}
