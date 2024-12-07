package com.qingyu.qingyupicturebackend.common;

import com.qingyu.qingyupicturebackend.exception.ErrorCode;

/**
 * @Description: 结果工具类，用于生成成功和失败的响应
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/7 下午8:58
 */
public class ResultUtils {

    /**
     * 成功响应
     *
     * @param data 成功数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, ErrorCode.SUCCESS.getMessage());
    }

    /**
     * 失败响应，使用错误码
     *
     * @param errorCode 错误码
     * @return 失败响应对象
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败响应，使用自定义的响应码和消息
     *
     * @param code    响应码
     * @param message 消息
     * @return 失败响应对象
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败响应，使用错误码和自定义消息
     *
     * @param errorCode 错误码
     * @param message   自定义消息
     * @return 失败响应对象
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
