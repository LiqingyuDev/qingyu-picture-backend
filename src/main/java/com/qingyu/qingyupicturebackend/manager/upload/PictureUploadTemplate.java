
package com.qingyu.qingyupicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.qingyu.qingyupicturebackend.config.CosClientConfig;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.manager.CosManager;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;

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
     * @param inputSource      文件来源对象，可以是 MultipartFile 或其他类型的文件输入源。
     * @param uploadPathPrefix 上传路径前缀。
     * @return 上传图片的结果。
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验图片
        validateFile(inputSource);

        String uuidPrefix = RandomUtil.randomString(16); // 随机字符串

        String originalFilename = getOriginalFilename(inputSource);
        String originalSuffix = FileUtil.getSuffix(originalFilename);
        String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss"); // 更精确的时间戳格式
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
            // 获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 获取图片处理结果
            ProcessResults processResultList = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResultList.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 从格式转换列表获取 CIObject 对象
                CIObject compressedCiObject = objectList.size() > 0 ? objectList.get(0) : null; // 压缩
                CIObject thumbnailCiObject = objectList.size() > 1 ? objectList.get(1) : null; // 缩略图

                // 如果没有压缩图，将缩略图作为默认的压缩图
                if (thumbnailCiObject == null) {
                    thumbnailCiObject = compressedCiObject;
                }


                // 封装返回结果
                String originalUrl = cosClientConfig.getHost() + uploadPath;
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject, originalUrl);
            }
            String originalUrl = cosClientConfig.getHost() + uploadPath;

            // 封装返回结果
            return buildResult(originalFilename, file, uploadPath, imageInfo, originalUrl);
        } catch (IOException e) {
            // 捕获并处理 IO 异常
            log.error("文件上传失败, filepath:{}", uploadPath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 临时文件清理
            clearTempFile(file);
        }
    }

    /**
     * 构建上传图片的结果，使用 CIObject 对象中的信息。
     *
     * @param originalFilename   原始文件名。
     * @param compressedCiObject CIObject 对象，包含图片处理后的信息。
     * @param thumbnailCiObject  CIObject 对象，包含缩略图信息。
     * @param originalUrl        原始图片的 URL。
     * @return 上传图片的结果。
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, String originalUrl) {
        return buildResult(originalFilename, compressedCiObject.getFormat(), compressedCiObject.getWidth(), compressedCiObject.getHeight(), compressedCiObject.getSize(), compressedCiObject.getKey(), thumbnailCiObject.getKey(), originalUrl);
    }

    /**
     * 构建上传图片的结果，使用本地文件和上传路径信息。
     *
     * @param originalFilename 原始文件名。
     * @param file             本地文件对象。
     * @param uploadPath       上传路径。
     * @param imageInfo        ImageInfo 对象，包含图片的基本信息。
     * @param originalUrl      原始图片的 URL。
     * @return 上传图片的结果。
     */
    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo, String originalUrl) {
        return buildResult(originalFilename, imageInfo.getFormat(), imageInfo.getWidth(), imageInfo.getHeight(), file.length(), uploadPath, null, originalUrl);
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
     * 构建上传图片的结果。
     *
     * @param originalFilename 原始文件名。
     * @param format           图片格式。
     * @param width            图片宽度。
     * @param height           图片高度。
     * @param size             图片大小。
     * @param urlPath          图片的 URL 路径。
     * @param thumbnailKey     缩略图的 key。
     * @param originalUrl      原始图片的 URL。
     * @return 上传图片的结果。
     */
    private UploadPictureResult buildResult(String originalFilename, String format, int width, int height, long size, String urlPath, String thumbnailKey, String originalUrl) {
        // 计算宽高比
        double picScale = 0;
        if (height != 0) { // 避免除以零的情况
            // 使用 NumberUtil 对宽高比进行四舍五入，保留两位小数
            picScale = NumberUtil.round(width * 1.0 / height, 2).doubleValue();
            log.info("四舍五入后的宽高比: " + picScale);
        } else {
            log.info("高度不能为零，无法计算宽高比。");
        }

        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + urlPath);
        uploadPictureResult.setOriginalUrl(originalUrl);
        log.info("上传成功, url:{}", uploadPictureResult.getUrl());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(size);
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(format);
        if (thumbnailKey != null) {
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailKey);
        }
        return uploadPictureResult;
    }

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
