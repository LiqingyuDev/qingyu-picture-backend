package com.qingyu.qingyupicturebackend.model.request;

import lombok.Data;

/**
 * @Description: 分页请求类
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/8 上午7:58
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

