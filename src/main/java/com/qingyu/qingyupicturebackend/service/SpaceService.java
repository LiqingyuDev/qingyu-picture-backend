package com.qingyu.qingyupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyu.qingyupicturebackend.model.dto.space.SpaceAddRequest;
import com.qingyu.qingyupicturebackend.model.dto.space.SpaceQueryRequest;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qingyu
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-01-04 19:33:00
 */
public interface SpaceService extends IService<Space> {

    Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 根据查询请求构建查询条件。
     *
     * @param spaceQueryRequest 空间查询请求参数，包含查询条件如标题、标签等
     * @return 返回封装了查询条件的 QueryWrapper 对象
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 将数据库中的空间实体转换为视图对象。
     *
     * @param space   数据库中的空间实体
     * @param request HTTP 请求对象，用于获取上下文信息
     * @return 返回空间视图对象（SpaceVO）
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页查询空间，并将结果转换为视图对象列表。
     *
     * @param spacePage 分页查询结果
     * @param request   HTTP 请求对象，用于获取上下文信息
     * @return 返回分页后的空间视图对象列表
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 验证空间信息是否合法。
     *
     * @param space 待验证的空间实体
     * @param add   是否为添加操作，true 表示添加操作，false 表示更新操作
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据用户级别填充空间等级。
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);


}
