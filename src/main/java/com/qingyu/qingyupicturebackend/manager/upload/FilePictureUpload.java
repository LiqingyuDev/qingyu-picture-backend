package com.qingyu.qingyupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import java.util.Objects;

/**
 * @Description: 通过文件上传图片的实现类。处理基于 MultipartFile 的图片上传逻辑，包括文件转移、获取原始文件名和文件校验。
 *
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/29 下午12:50
 */
@Service

public class FilePictureUpload extends PictureUploadTemplate {


    /**
     * 将上传的文件内容转移到临时文件。
     *
     * @param inputSource 文件来源对象，必须是 MultipartFile 类型。
     * @param file        目标临时文件
     */
    @Override
    protected void processFile(Object inputSource, File file) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件上传失败");
        }
    }

    /**
     * 获取文件的原始名称（不包含路径）。
     *
     * @param inputSource 文件来源对象，必须是 MultipartFile 类型。
     * @return 文件的原始名称
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    /**
     * 校验文件的有效性，包括文件是否为空、文件大小和文件格式。
     *
     * @param inputSource 文件来源对象，必须是 MultipartFile 类型。
     */
    @Override
    protected void validateFile(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;

        // 校验文件是否为空
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");

        // 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过2M");

        // 校验文件格式
        String fileSuffix = Objects.requireNonNull(FileUtil.getSuffix(multipartFile.getOriginalFilename())).toLowerCase();
        ThrowUtils.throwIf(!ALLOWED_SUFFIXES.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }
}
