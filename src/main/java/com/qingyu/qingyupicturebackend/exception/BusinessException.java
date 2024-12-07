package com.qingyu.qingyupicturebackend.exception;

import lombok.Getter;

/**
 * @Description: 通用异常返回
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/7 下午8:39
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误码
     * -- GETTER --
     *  获取错误码
     *
     * @return 错误码

     */
    private final int code;

    /**
     * 构造方法，使用错误码和消息创建异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法，使用 ErrorCode 枚举创建异常
     *
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法，使用 ErrorCode 枚举和自定义消息创建异常
     *
     * @param errorCode 错误码枚举
     * @param message   自定义错误消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
