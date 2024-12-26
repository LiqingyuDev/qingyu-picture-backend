package com.qingyu.qingyupicturebackend.model.enums;

import lombok.Getter;

/**
 * @Description: 图片审核状态枚举
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/25 下午7:28
 */
@Getter
public enum PictureReviewStatuesEnum {
    /**
     * 状态：0-待审核; 1-通过; 2-拒绝
     */
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    /**
     * 构造方法，初始化角色的文本描述和值
     *
     * @param text  角色的文本描述
     * @param value 角色的值
     */
    PictureReviewStatuesEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据角色值获取对应的枚举对象
     *
     * @param value 角色的值
     * @return 对应的枚举对象，如果没有找到则返回null
     */
    public static PictureReviewStatuesEnum getEnumByValue(int value) {
        for (PictureReviewStatuesEnum pictureReviewStatuesEnum : PictureReviewStatuesEnum.values()) {
            if (pictureReviewStatuesEnum.value == value) {
                return pictureReviewStatuesEnum;
            }
        }
        return null;
    }
}
