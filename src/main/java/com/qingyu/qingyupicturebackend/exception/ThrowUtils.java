package com.qingyu.qingyupicturebackend.exception;

/**
 * @Description: 异常处理工具类
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/7 下午8:42
 */
public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition        条件
     * @param runtimeException 异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param errorCode 错误码
     * @param message 错误消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode,String message) {
        throwIf(condition, new BusinessException(errorCode,message));
    }
}
