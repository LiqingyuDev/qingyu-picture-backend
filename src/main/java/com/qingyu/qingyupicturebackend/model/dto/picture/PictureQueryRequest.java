package com.qingyu.qingyupicturebackend.model.dto.picture;

import com.qingyu.qingyupicturebackend.model.request.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Description: 查询图片请求类
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/20 上午9:46
 */
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签（数据库中是JSON 数组,为了前端方便像前端发List）
     */
    private List<String> tags;

    /**
     * 图片体积
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 空间id
     */
    private Long spaceId;
    /**
     * 是否只查询 spaceId 为 null 的数据(默认公开数据)
     */
    private Boolean nullSpaceId = true;
    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;
// --------------------图片添加按照照片编辑时间筛选---------------------------------------------
    /**
     * 开始编辑时间
     */
    private Date startEditTime;

    /**
     * 结束编辑时间
     */
    private Date endEditTime;

// ---------------------------------------------------------------------------------------------------------------------
    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;


    private static final long serialVersionUID = 1L;


}
