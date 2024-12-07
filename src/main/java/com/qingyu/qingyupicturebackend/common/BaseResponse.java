package com.qingyu.qingyupicturebackend.common;

import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import lombok.Data;

/**
 * @Description: 通用响应类
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/7 下午8:48
 */
@Data
public class BaseResponse<T> {
    /**
     * 响应码
     */
    private int code;

    /**
     * 数据
     */
    private T data;

    /**
     * 消息
     */
    private String message;

    /**
     * 构造方法，使用响应码、数据和消息创建响应
     *
     * @param code    响应码
     * @param data    数据
     * @param message 消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 构造方法，使用响应码和数据创建响应，默认消息为空字符串
     *
     * @param code 响应码
     * @param data 数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 构造方法，使用错误码创建响应
     *
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }


}
