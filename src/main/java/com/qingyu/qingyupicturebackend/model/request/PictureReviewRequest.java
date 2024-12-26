package com.qingyu.qingyupicturebackend.model.request;

import lombok.Data;

/**
 * @Description: 管理员审核图片请求体
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/26 上午7:58
 */
@Data
public class PictureReviewRequest {
    /**
     * 图片id
     */
    private Long id;
    /**
     * 审核状态 : 0:待审核 1:通过 2:拒绝
     */
    private Integer reviewStatus;
    /**
     * 审核信息
     */
    private String reviewMessage;
}
