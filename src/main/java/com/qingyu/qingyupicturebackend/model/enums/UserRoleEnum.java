package com.qingyu.qingyupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author liqingyu.dev@gmail.com
 * @since 2024/12/9 下午8:10
 */
@Getter
public enum UserRoleEnum {

    /**
     * 普通用户角色
     */
    USER("用户", "user"),

    /**
     * 管理员角色
     */
    ADMIN("管理员", "admin");

    /**
     * 角色的文本描述
     */
    private final String text;

    /**
     * 角色的值
     */
    private final String value;

    /**
     * 构造方法，初始化角色的文本描述和值
     *
     * @param text 角色的文本描述
     * @param value 角色的值
     */
    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据角色值获取对应的枚举对象
     *
     * @param value 角色的值
     * @return 对应的枚举对象，如果没有找到则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            if (userRoleEnum.value.equals(value)) {
                return userRoleEnum;
            }
        }
        return null;
    }
}
