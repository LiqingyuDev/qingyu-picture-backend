package com.qingyu.qingyupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 通过 URL 上传图片的实现类。
 * 处理基于 URL 的图片下载逻辑，包括文件下载、获取原始文件名和文件校验。
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/29 下午1:18
 */
@Slf4j
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    // 允许上传的文件后缀列表
    private static final List<String> ALLOWED_SUFFIXES = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");

    // 文件最大大小（2MB）
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 2;

    /**
     * 下载文件到临时文件。
     *
     * @param inputSource 文件来源对象，必须是 URL 字符串。
     * @param file        目标临时文件
     */
    @Override
    protected void processFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }

    /**
     * 获取文件的原始名称（不包含路径）。
     *
     * @param inputSource 文件来源对象，必须是 URL 字符串。
     * @return 文件的原始名称
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        //带着格式后缀
        return FileUtil.getName(fileUrl);
    }

    /**
     * 校验文件的有效性，包括 URL 格式、协议、文件类型和文件大小。
     *
     * @param inputSource 文件来源对象，必须是 URL 字符串。
     */
    @Override
    protected void validateFile(Object inputSource) {
        String fileUrl = (String) inputSource;

        // 校验 URL 是否为空
        ThrowUtils.throwIf(StringUtils.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "URL 为空");

        // 校验 URL 格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "URL 格式错误");
        }

        // 校验 URL 协议
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "URL 协议错误");
        }

        // 发送 HEAD 请求以获取文件详细信息
        HttpRequest request = HttpUtil.createRequest(Method.HEAD, fileUrl);
        HttpResponse response = null;
        try {
            response = request.execute();
            int statusCode = response.getStatus();  // 获取 HTTP 状态码
            if (statusCode != HttpStatus.OK.value()) {
                // 如果状态码不是 200，直接返回（有可能不支持 HEAD 请求）
                return;
            }

            // 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                if (!ALLOWED_SUFFIXES.contains(contentType.toLowerCase())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型不合规");
                }
            }

            // 校验文件大小
            long contentLength = response.contentLength();
            if (contentLength > MAX_FILE_SIZE) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小超过2M");
            }

        } catch (Exception e) {
            log.error("请求过程中发生异常: {}", e.getMessage(), e);
        } finally {
            // 确保响应对象被关闭
            if (response != null) {
                response.close();
            }
        }
    }
}
