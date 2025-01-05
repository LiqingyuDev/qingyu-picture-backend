package com.qingyu.qingyupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyu.qingyupicturebackend.annotation.AuthCheck;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.space.*;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.SpaceVO;
import com.qingyu.qingyupicturebackend.service.SpaceService;
import com.qingyu.qingyupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author qingyu
 * @description 针对space(空间)的控制器
 * @createDate 2025-01-05 11:37
 */
@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;

    // region: 增删查改

    /**
     * 创建空间。
     *
     * @param spaceAddRequest 包含创建空间所需参数的对象
     * @param request         当前的HTTP请求对象，用于获取登录用户信息
     * @return 返回包含创建成功空间信息的 {@link BaseResponse<SpaceVO>} 对象
     * @throws BusinessException 如果用户未登录或创建失败
     */
    @PostMapping("/add")
    public BaseResponse<SpaceVO> createSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 将请求参数转换为实体对象
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        space.setUserId(loginUser.getId());
        space.setCreateTime(new Date());
        space.setEditTime(new Date());

        // 校验空间数据的有效性
        spaceService.validSpace(space, true);

        // 创建空间
        boolean saveResult = spaceService.save(space);
        ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR, "创建失败");

        // 获取创建后的空间信息
        Space createdSpace = spaceService.getById(space.getId());
        SpaceVO spaceVO = spaceService.getSpaceVO(createdSpace, request);

        return ResultUtils.success(spaceVO);
    }

    /**
     * 根据 ID 删除空间。
     *
     * @param spaceDeleteRequest 包含要删除空间的 ID 和用户 ID 的请求体
     * @param request            HTTP 请求对象，用于获取当前登录用户信息
     * @return 操作结果
     * @throws BusinessException 如果用户未登录或无权限删除
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody SpaceDeleteRequest spaceDeleteRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 取出参数
        Long id = spaceDeleteRequest.getId();
        Long userId = spaceDeleteRequest.getUserId();

        // 仅本人和管理员可以删除
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }

        // 删除空间
        boolean removeById = spaceService.removeById(id);
        return ResultUtils.success(removeById);
    }

    /**
     * 【管理员】更新空间信息。
     *
     * @param spaceUpdateRequest 包含更新信息的请求对象
     * @return 更新操作的结果响应
     * @throws BusinessException 如果请求参数为空或空间不存在
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        // 校验
        ThrowUtils.throwIf(spaceUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(spaceService.getById(spaceUpdateRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的空间不存在");

        // 将请求参数转换为实体对象
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);

        // 自动填充空间级别相关数据
        spaceService.fillSpaceBySpaceLevel(space);

        // 空间数据校验
        spaceService.validSpace(space, false);
        //判断是否存在
        ThrowUtils.throwIf(spaceService.getById(space.getId()) == null, ErrorCode.NOT_FOUND_ERROR, "修改的空间不存在");

        // 更新空间信息到数据库
        boolean updatedById = spaceService.updateById(space);
        ThrowUtils.throwIf(!updatedById, ErrorCode.OPERATION_ERROR, "更新失败");

        // 返回成功响应
        return ResultUtils.success(true);
    }

    /**
     * 【用户】更新空间信息。
     *
     * @param spaceEditRequest 包含更新信息的请求对象
     * @param request          HTTP 请求对象，用于获取当前登录用户信息
     * @return 更新操作的结果响应
     * @throws BusinessException 如果请求参数为空、空间不存在或无权限修改
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(spaceService.getById(spaceEditRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的空间不存在");

        // dto转换entity
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);

        // 更新编辑时间
        space.setEditTime(new Date());

        // 自动填充空间级别相关数据
        spaceService.fillSpaceBySpaceLevel(space);

        // 空间数据校验
        spaceService.validSpace(space, false);

        // 仅本人或管理员可以修改
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (!loginUserId.equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
        }

        // 操作数据库
        boolean updatedById = spaceService.updateById(space);
        ThrowUtils.throwIf(!updatedById, ErrorCode.OPERATION_ERROR, "更新失败");

        return ResultUtils.success(true);
    }

    /**
     * 管理员根据 ID 获取空间（不需要脱敏）。
     *
     * @param id 空间的唯一标识符
     * @return 包含空间对象的成功响应
     * @throws BusinessException 如果空间 ID 无效或空间不存在
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(@RequestParam long id) {
        // 校验参数是否合法
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "无效的空间 ID");

        // 查询数据库获取空间信息
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        // 返回查询到的空间对象
        return ResultUtils.success(space);
    }

    /**
     * 根据 ID 获取空间（需要脱敏）。
     *
     * @param id      空间的唯一标识符
     * @param request HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含脱敏后的空间对象的成功响应
     * @throws BusinessException 如果空间 ID 无效或空间不存在
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(@RequestParam long id, HttpServletRequest request) {
        // 校验参数是否合法
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "无效的空间 ID");

        // 查询数据库获取空间信息
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

        // 将空间信息转换为脱敏后的视图对象 (VO)
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);

        // 返回脱敏后的空间对象
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间列表（需要脱敏和限制条数）。
     *
     * @param spaceQueryRequest 包含分页和查询条件的请求对象
     * @param request           HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含分页结果的成功响应
     * @throws BusinessException 如果请求参数为空或每页显示的数据超过20条
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 获取分页参数
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);

        // 限制每页最多显示20条数据，防止爬虫滥用
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多显示 20 条数据");

        // 获取分页数据
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), queryWrapper);
        Page<SpaceVO> spaceVOPage = spaceService.getSpaceVOPage(spacePage, request);

        // 返回查询结果
        return ResultUtils.success(spaceVOPage);
    }

    /**
     * 【管理员】分页获取空间列表（不需要脱敏和限制条数，不使用缓存）。
     *
     * @param spaceQueryRequest 包含分页和查询条件的请求对象
     * @return 包含分页结果的成功响应
     * @throws BusinessException 如果请求参数为空
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        // 校验参数
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 取值
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        QueryWrapper<Space> queryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);

        // 查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), queryWrapper);

        return ResultUtils.success(spacePage);
    }

    // endregion: 增删查改
}
