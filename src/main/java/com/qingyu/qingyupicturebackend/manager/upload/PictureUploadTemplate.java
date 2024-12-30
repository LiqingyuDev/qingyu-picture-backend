package com.qingyu.qingyupicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qingyu.qingyupicturebackend.config.CosClientConfig;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.manager.CosManager;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 通篇上传模板方法
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/18 下午8:46
 */
@Slf4j
public abstract class PictureUploadTemplate {
    // 允许上传的文件后缀列表
    protected static final List<String> ALLOWED_SUFFIXES = Arrays.asList("jpeg", "png", "jpg", "webp");
    // 文件最大大小（2MB）
    protected static final long MAX_FILE_SIZE = 1024 * 1024 * 2;
    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //MultipartFile multipartFile = (MultipartFile) inputSource;
        // 校验图片
        validateFile(inputSource);

        String uuidPrefix = RandomUtil.randomString(16); // 随机字符串

        String originalFilename = getOriginalFilename(inputSource);
        String originalSuffix = FileUtil.getSuffix(originalFilename);
        String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss");// 更精确的时间戳格式
        String uploadFileName = String.format("%s_%s.%s", timestamp, uuidPrefix, originalSuffix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源
            processFile(inputSource, file);

            // 将临时文件上传到 COS
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //计算宽高比例
            String format = imageInfo.getFormat();
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();

// 计算宽高比
            double picScale = 0;
            if (height != 0) { // 避免除以零的情况
                // 使用 NumberUtil 对宽高比进行四舍五入，保留两位小数
                picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
                log.info("四舍五入后的宽高比: " + picScale);
            } else {
                log.info("高度不能为零，无法计算宽高比。");
            }
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
            log.info("上传成功,url:{}", uploadPictureResult.getUrl());
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);
            // 返回文件地址
            return uploadPictureResult;
        } catch (IOException e) {
            // 捕获并处理 IO 异常
            log.error("文件上传失败,filepath:{}", uploadPath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //临时文件清理
            clearTempFile(file);
        }
    }

    /**
     * 处理文件来源，并将上传的文件内容转移到临时文件。
     *
     * @param inputSource 文件来源对象，可以是 MultipartFile 或其他类型(URL)的文件输入源。
     * @param file
     */
    protected abstract void processFile(Object inputSource, File file);

    /**
     * 获取文件的原始名称。
     *
     * @param inputSource 文件来源对象，可以是 MultipartFile 或其他类型的文件输入源。
     * @return 文件的原始名称（不包含路径）。
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验文件的有效性，包括文件是否为空、文件大小和文件格式等。
     *
     * @param inputSource 文件来源对象，可以是 MultipartFile 或其他类型的文件输入源。
     */
    protected abstract void validateFile(Object inputSource);


    /**
     * 清除临时文件
     */
    private void clearTempFile(File file) {
        // 清理临时文件
        if (file == null) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            log.warn("临时文件删除失败, filepath: {}", file.getAbsolutePath());

        }
    }


}