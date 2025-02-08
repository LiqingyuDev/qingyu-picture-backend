package com.qingyu.qingyupicturebackend.api.aliyunai.outPainting;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model.CreateOutPaintingTaskRequest;
import com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model.CreateOutPaintingTaskResponse;
import com.qingyu.qingyupicturebackend.api.aliyunai.outPainting.model.GetOutPaintingTaskResponse;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author qingyu
 * @description 百炼大模型调用API
 * @date 2025/2/4 11:51
 */

@Component
@Slf4j
public class AliYunAiApi {

    // 百炼大模型调用apiKey
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    //创建任务地址常量
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";
    // 查询任务结果地址常量
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     * <p>
     * POST https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting
     */
    public CreateOutPaintingTaskResponse createImageOutPainting(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        ThrowUtils.throwIf(createOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR, "AI扩图请求参数为空");

        //发送请求
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                //密钥
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                //异步
                .header("X-DashScope-Async", "enable")
                //类型
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                //请求体
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        log.info("请求参数：{}", JSONUtil.toJsonStr(createOutPaintingTaskRequest));

        try (HttpResponse httpResponse = httpRequest.execute()) {
            if (!httpResponse.isOk()) {
                log.error("请求失败，状态码：{}，响应内容：{}", httpResponse.getStatus(), httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图请求失败，状态码：" + httpResponse.getStatus());
            }
            //校验响应
            CreateOutPaintingTaskResponse responseBean = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            //接口错误码。接口成功请求不会返回该参数。
            String errorCode = responseBean.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = responseBean.getMessage();
                log.error("请求失败，错误码：{}，错误信息：{}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图请求失败，错误码：" + errorCode + "，错误信息：" + errorMessage);
            }
            return responseBean;

        }
    }


    /**
     * 根据任务ID查询结果
     * <p>
     * GET https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}
     */
    public GetOutPaintingTaskResponse getImageOutPainting(String taskId) {
        ThrowUtils.throwIf(taskId == null, ErrorCode.PARAMS_ERROR, "AI扩图任务ID为空");

        // 构建请求
        String requestUrl = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        try (HttpResponse httpResponse = HttpRequest.get(requestUrl)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .execute()) {

            if (!httpResponse.isOk()) {
                log.error("请求失败，状态码：{}，响应内容：{}", httpResponse.getStatus(), httpResponse.body());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图请求失败，状态码：" + httpResponse.getStatus());
            }

            GetOutPaintingTaskResponse responseBean = JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
            return responseBean;
        }
    }


}
