package com.qingyu.qingyupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * @author qingyu
 * @description 空间类型枚举类
 * @date 2025/2/9 12:25
 */
@Getter
public enum SpaceTypeEnum {

    /**
     * 私有空间
     */
    PRIVATE("私有空间", 0),

    /**
     * 团队空间
     */
    TEAM("团队空间", 1);

    private final String text;
    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据值获取枚举对象
     * @param value 枚举值
     * @return 对应的枚举对象，如果不存在则返回空Optional
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value == value) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}
