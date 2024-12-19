package com.qingyu.qingyupicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qingyu.qingyupicturebackend.config.CosClientConfig;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @Description: 对cos进一步封装(和业务逻辑有点关系)
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/18 下午8:46
 */
@Slf4j
@Service
public class FileManager {
    @Resource
    private CosManager cosManager;
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validateFile(multipartFile);

        String uuidPrefix = RandomUtil.randomString(16); // 随机字符串
        String originalFilename = multipartFile.getOriginalFilename();
        String originalSuffix = FileUtil.getSuffix(originalFilename);
        String timestamp = DateUtil.format(new Date(), "yyyyMMddHHmmss");// 更精确的时间戳格式
        String uploadFileName = String.format("%s_%s.%s", timestamp, uuidPrefix, originalSuffix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);

        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 将上传的文件内容转移到临时文件
            multipartFile.transferTo(file);
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
     * 校验图片
     *
     * @param multipartFile
     */
    private void validateFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件为空");
        // 1. 校验大小
        long picSize = multipartFile.getSize();
        final long MAX_SIZE = 1024 * 1024 * 2; // 2MB
        ThrowUtils.throwIf(picSize > MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小超过2M");

        // 2. 校验格式
        String fileSuffix = Objects.requireNonNull(FileUtil.getSuffix(multipartFile.getOriginalFilename())).toLowerCase(); // 获取文件后缀并转换为小写
        // 允许上传的文件后缀列表
        final List<String> ALLOW_Suffix_LIST = Arrays.asList("jpeg", "png", "jpg", "webp");
        ThrowUtils.throwIf(!ALLOW_Suffix_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
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