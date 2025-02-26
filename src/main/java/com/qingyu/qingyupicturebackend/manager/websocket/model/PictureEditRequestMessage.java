package com.qingyu.qingyupicturebackend.manager.websocket.model;

import lombok.Data;

/**
 * 图片编辑消息请求
 * @author qingyu
 * @date 2025/2/25 10:44
 */
@Data
public class PictureEditRequestMessage {
    /**
     * 消息类型
     */
    private String type;

    /**
     * 执行的动作
     */
    private String editAction;
}
