package com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 响应体类，用于封装根据任务ID查询结果的任务信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetOutPaintingTaskResponse implements Serializable {

    /**
     * 请求唯一标识。可用于请求明细溯源和问题排查。
     */
    private String requestId;

    /**
     * 输出的任务信息。
     */
    private Output output;

    /**
     * 图像统计信息。
     */
    private Usage usage;

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

        /**
         * 任务统计信息。
         */
        private TaskMetrics taskMetrics;



        /**
         * 任务提交时间。
         */
        private String submitTime;

        /**
         * 任务执行时间。
         */
        private String scheduledTime;

        /**
         * 任务完成时间。
         */
        private String endTime;
        /**
         * 输出图像URL地址。
         */
        private String outputImageUrl;
        /**
         * 接口错误码。接口成功请求不会返回该参数。
         */
        private String code;
        /**
         * 接口错误信息。接口成功请求不会返回该参数。
         */
        private String message;
    }
    /**
     * 任务统计信息。
     */
    @Data
    public static class TaskMetrics implements Serializable {

        /**
         * 总的任务数。
         */
        private int total;

        /**
         * 任务状态为成功的任务数。
         */
        private int succeeded;

        /**
         * 任务状态为失败的任务数。
         */
        private int failed;

    }

    /**
     * 图像统计信息。
     */
    @Data
    public static class Usage implements Serializable {
        /**
         * 生成图像的数量。
         */
        private Integer imageCount;
    }
}
