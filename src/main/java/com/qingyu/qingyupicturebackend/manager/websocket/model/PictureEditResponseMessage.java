package com.qingyu.qingyupicturebackend.manager.websocket.model;

import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import lombok.Data;

/**
 * 图片编辑响应消息
 * @author qingyu
 * @date 2025/2/25 10:49
 */
@Data
public class PictureEditResponseMessage {
    /**
     * 消息类型
     */
    private String type;
    /**
     * 消息
     */
    private String message;

    /**
     * 其他用户执行的动作
     */
    private String editAction;
    /**
     *用户信息
     */
    private UserVO user;
}
