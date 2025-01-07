package com.qingyu.qingyupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 管理员编辑请求
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/20 上午9:52
 */
@Data
public class PictureUpdateRequest implements Serializable {
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


    private static final long serialVersionUID = 1L;
}
