package com.qingyu.qingyupicturebackend.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author qingyu
 * @description: 帮前端查询空间级别的列表
 * @date 2025/1/7 20:03
 */
@Data
@AllArgsConstructor
public class SpaceLevel {
    /**
     * 空间级别的唯一标识值
     */
    private int value;

    /**
     * 空间级别的文本描述
     */
    private String text;

    /**
     * 空间级别的最大上传次数
     */
    private long maxCount;

    /**
     * 空间级别的最大上传大小（字节）
     */
    private long maxSize;
}
