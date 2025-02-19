package com.qingyu.qingyupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.qingyu.qingyupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.SpaceUser;
import com.qingyu.qingyupicturebackend.model.vo.SpaceUserVO;

import java.util.List;

/**
 * 空间用户关联服务接口，用于对空间用户关联表【spaceUser_user】进行数据库操作。
 *
 * @author qingyu
 * @since 2025-02-09
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 在指定空间内添加新用户。
     *
     * @param spaceUserAddRequest 包含要添加的用户信息和空间信息的请求对象
     * @return 数据库返回的主键ID
     */
    Long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 根据查询请求构建查询条件。
     *
     * @param spaceUserQueryRequest 查询请求参数，包含空间信息和用户信息等查询条件
     * @return 封装了查询条件的 QueryWrapper 对象
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 将数据库中的空间用户关联实体转换为视图对象。
     *
     * @param spaceUser 数据库中的空间用户关联实体
     * @return 转换后的空间用户关联视图对象（SpaceUserVO）
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser);

    /**
     * 查询空间用户关联，并将结果转换为视图对象列表。
     *
     * @param spaceUserList 空间用户关联实体列表
     * @return 转换后的空间用户关联视图对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 校验空间用户关联对象的有效性。
     *
     * @param spaceUser 待验证的空间用户关联实体
     * @param add       是否为添加操作，true 表示添加操作，false 表示更新操作
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);
}
