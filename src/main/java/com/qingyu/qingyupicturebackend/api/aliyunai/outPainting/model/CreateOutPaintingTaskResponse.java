package com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应体类，用于封装阿里云AI的图像画面扩展请求的响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOutPaintingTaskResponse implements Serializable {

    /**
     * 请求唯一标识。可用于请求明细溯源和问题排查。
     */

    private String requestId;

    /**
     * 输出的任务信息。
     */
    private Output output;

    /**
     * 接口错误码。接口成功请求不会返回该参数。
     */
    private String code;

    /**
     * 接口错误信息。接口成功请求不会返回该参数。
     */
    private String message;

    /**
     * 输出的任务信息。
     */
    @Data
    public static class Output implements Serializable {
        /**
         * 任务id。
         */

        private String taskId;

        /**
         * 任务状态。
         * PENDING：排队中
         * RUNNING：处理中
         * SUSPENDED：挂起
         * SUCCEEDED：执行成功
         * FAILED：执行失败
         * UNKNOWN：任务不存在或状态未知
         */
        private String taskStatus;
    }
}
