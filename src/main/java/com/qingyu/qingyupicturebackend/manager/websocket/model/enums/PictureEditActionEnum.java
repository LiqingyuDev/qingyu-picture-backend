package com.qingyu.qingyupicturebackend.manager.websocket.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片编辑操作枚举
 *
 * @author qingyu
 * @date 2025/2/25 12:12
 */
@Getter
@AllArgsConstructor
public enum PictureEditActionEnum {

    ZOOM_IN("放大操作", "ZOOM_IN"),
    ZOOM_OUT("缩小操作", "ZOOM_OUT"),
    ROTATE_LEFT("左旋操作", "ROTATE_LEFT"),
    ROTATE_RIGHT("右旋操作", "ROTATE_RIGHT");

    private final String text;
    private final String value;

    /**
     * 通过value获取枚举
     *
     * @param value
     * @return
     */
    public static PictureEditActionEnum getEnumByValue(String value) {
        for (PictureEditActionEnum actionEnum : values()) {
            if (actionEnum.getValue().equals(value)) {
                return actionEnum;
            }
        }
        return null;
    }
}
