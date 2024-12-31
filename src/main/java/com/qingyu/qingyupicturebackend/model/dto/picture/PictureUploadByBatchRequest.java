package com.qingyu.qingyupicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 批量爬取图片请求体
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/30 下午2:55
 */
@Data
public class PictureUploadByBatchRequest implements Serializable {

    /**
     * 爬取图片关键词
     */
    private String searchText;
    /**
     * 名称前缀
     */
    private String namePrefix;
    /**
     * 爬取图片名称
     */
    private String picName;

    /**
     * 爬取图片数量
     */
    private Integer count = 10;

    private static final long serialVersionUID = 1L;
}
