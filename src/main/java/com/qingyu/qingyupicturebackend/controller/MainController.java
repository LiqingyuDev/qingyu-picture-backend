package com.qingyu.qingyupicturebackend.controller;

import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 主控制器
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/7 下午9:12
 */
@RestController
public class MainController {

    /**
     * 健康检查
     *
     * @return 健康检查结果
     */
    @GetMapping("/health")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}
