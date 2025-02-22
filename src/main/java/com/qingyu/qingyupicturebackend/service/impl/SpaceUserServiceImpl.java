package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.mapper.SpaceUserMapper;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.SpaceUser;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.SpaceUserVO;
import com.qingyu.qingyupicturebackend.service.SpaceService;
import com.qingyu.qingyupicturebackend.service.SpaceUserService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qingyu
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-02-09 11:54:02
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {
    @Resource
    private UserService userService;
    @Lazy
    @Resource
    private SpaceService spaceService;

    /**
     * 在指定空间内添加新用户。
     *
     * @param spaceUserAddRequest 包含要添加的用户信息和空间信息的请求对象
     * @return 数据库返回的主键ID
     */
    @Override
    public Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        //保存到数据库
        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "添加失败");
        return spaceUser.getId();
    }

    /**
     * 根据查询请求构建查询条件。
     *
     * @param spaceUserQueryRequest 查询请求参数，包含空间信息和用户信息等查询条件
     * @return 封装了查询条件的 QueryWrapper 对象
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 从请求参数中获取查询条件
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        // 创建一个 QueryWrapper 对象，用于构建查询条件
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(StrUtil.isNotBlank(spaceRole), "spaceRole", spaceRole);

        return queryWrapper;

    }

    /**
     * 将数据库中的空间用户关联实体转换为视图对象。
     *
     * @param spaceUser 数据库中的空间用户关联实体
     * @return 转换后的空间用户关联视图对象（SpaceUserVO）
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser) {
        //共有字段
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        //封装类关联额外字段
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();

        spaceUserVO.setUser(userService.getUserVO(userService.getById(userId)));
        spaceUserVO.setSpace(spaceService.getSpaceVO(spaceService.getById(spaceId)));

        return spaceUserVO;
    }

    /**
     * 查询空间用户关联，并将结果转换为视图对象列表。
     *
     * @param spaceUserList 空间用户关联实体列表
     * @return 转换后的空间用户关联视图对象列表
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        //转化成封装列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(this::getSpaceUserVO)
                .collect(Collectors.toList());
        //收集需要关联的用户空间信息
        Set<Long> spaceIdList = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        Set<Long> userIdList = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        //批量查询用户和空间信息
        Map<Long, List<Space>> spaceListMap = spaceService
                .listByIds(spaceIdList)
                .stream()
                .collect(Collectors.groupingBy(Space::getId));
        Map<Long, List<User>> userListByUserId = userService
                .listByIds(userIdList)
                .stream()
                .collect(Collectors.groupingBy(User::getId));
        //填充封装类关联额外字段
        spaceUserVOList.forEach(spaceUserVO -> {
            Long spaceId = spaceUserVO.getSpaceId();
            if (spaceId != null && spaceListMap.containsKey(spaceId)) {
                Space space = spaceListMap.get(spaceId).get(0);
                spaceUserVO.setSpace(spaceService.getSpaceVO(space));
            }

            Long userId = spaceUserVO.getUserId();
            if (userId != null && userListByUserId.containsKey(userId)) {
                User user = userListByUserId.get(userId).get(0);
                spaceUserVO.setUser(userService.getUserVO(user));
            }
        });
        return spaceUserVOList;

    }

    /**
     * 校验空间用户关联对象的有效性。
     *
     * @param spaceUser 待验证的空间用户关联实体
     * @param add       是否为添加操作，true 表示添加操作，false 表示更新操作
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {

    }
}




