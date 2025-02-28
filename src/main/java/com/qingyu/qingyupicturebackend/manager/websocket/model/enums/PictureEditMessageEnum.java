package com.qingyu.qingyupicturebackend.manager.websocket.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片编辑消息枚举
 *
 * @author qingyu
 * @date 2025/2/25 11:51
 */
@AllArgsConstructor
@Getter
public enum PictureEditMessageEnum {
    /**
     * 消息类型，例如 "INFO", "ERROR", "ENTER_EDIT", "EXIT_EDIT", "EDIT_ACTION"
     */
    INFO("发送通知", "INFO"),
    ERROR("发送错误", "ERROR"),
    ENTER_EDIT("进入编辑", "ENTER_EDIT"),
    EXIT_EDIT("退出编辑", "EXIT_EDIT"),
    EDIT_ACTION("编辑操作", "EDIT_ACTION");

    private final String text;
    private final String value;

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditMessageEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            // 如果 value 为空，则返回 null
            return null;
        }

        for (PictureEditMessageEnum pictureEditMessageEnum : PictureEditMessageEnum.values()) {
            if (pictureEditMessageEnum.getValue().equals(value)) {
                return pictureEditMessageEnum;
            }
        }
        return null;
    }

}
