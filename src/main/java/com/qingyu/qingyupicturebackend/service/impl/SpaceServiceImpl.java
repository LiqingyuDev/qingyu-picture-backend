package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.mapper.SpaceMapper;
import com.qingyu.qingyupicturebackend.model.dto.space.SpaceQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.SpaceLevelEnum;
import com.qingyu.qingyupicturebackend.model.vo.SpaceVO;
import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import com.qingyu.qingyupicturebackend.service.SpaceService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qingyu
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2025-01-04 19:33:00
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {
    @Resource
    private UserService userService;

    /**
     * 构建空间查询条件。
     *
     * @param spaceQueryRequest 空间查询请求参数
     * @return 包含查询条件的 QueryWrapper 对象
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        ThrowUtils.throwIf(spaceQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 从请求参数中获取查询条件
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 创建一个 QueryWrapper 对象，用于构建查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotNull(spaceLevel), "spaceLevel", spaceLevel);
        // 根据排序字段，设置排序方式
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);

        return queryWrapper;
    }

    /**
     * 将数据库中的空间实体转换为视图对象，并关联用户信息。
     *
     * @param space   数据库中的空间实体
     * @param request HTTP 请求对象，用于获取上下文信息（如用户会话）
     * @return 转换后的 SpaceVO 对象
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }

        return spaceVO;
    }

    /**
     * 获取分页查询结果并将其转换为视图对象列表，同时关联用户信息。
     *
     * @param spacePage 分页查询结果
     * @param request   HTTP 请求对象，用于获取上下文信息（如用户会话）
     * @return 包含转换后视图对象的分页结果
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 -> 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 查询关联用户id集合
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userListByUserId = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));

        // 为每个 SpaceVO 设置关联的 UserVO
        for (SpaceVO spaceVO : spaceVOList) {
            Long userId = spaceVO.getUserId();
            if (userId != null && userListByUserId.containsKey(userId)) {
                User user = userListByUserId.get(userId).get(0);
                UserVO userVO = userService.getUserVO(user);
                spaceVO.setUser(userVO);
            }
        }

        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }


    /**
     * @param space 待验证的空间实体
     * @param add   是否为添加操作，true 表示添加操作，false 表示更新操作
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间为空");
        //提取属性
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);

        //校验属性
        //创建时:
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间等级为空");
        }
        //更新时:
        ThrowUtils.throwIf(spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称过长");
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "空间等级错误");
    }

    /**
     * 根据用户级别填充空间等级相关参数。
     *
     * @param space 空间实体对象，包含空间等级等信息
     * @throws IllegalArgumentException 如果传入的 {@code space} 参数为空或 {@code spaceLevel} 无效
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 检查传入参数是否为空
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR, "空间实体为空");

        Integer spaceLevel = space.getSpaceLevel();

        // 获取对应的空间等级枚举
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "无效的空间等级");

        // 如果空间等级为普通级别，则设置最大文件数量和最大存储容量
        if (SpaceLevelEnum.COMMON.equals(enumByValue)) {
            if (space.getMaxCount() == null){
                space.setMaxCount(enumByValue.getMaxCount());
            }
            if (space.getMaxSize() == null){
                space.setMaxSize(enumByValue.getMaxSize());
            }
        }
    }

}




