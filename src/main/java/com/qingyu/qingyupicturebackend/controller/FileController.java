package com.qingyu.qingyupicturebackend.controller;

import cn.hutool.core.io.IoUtil;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.qingyu.qingyupicturebackend.annotation.AuthCheck;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.manager.CosManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Description: 文件上传接口
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/17 下午8:42
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    /**
     * 注入 COS 管理类，用于处理文件上传到 COS
     */
    @Resource
    private CosManager cosManager;

    /**
     * 测试文件上传接口
     *
     * @param multipartFile 上传的文件
     * @return 响应结果，包含文件的存储路径
     */

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 获取文件名
        String filename = multipartFile.getOriginalFilename();
        // 拼接文件路径
        String filePath = "test/" + filename;
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(filePath, null);
            // 将上传的文件内容转移到临时文件
            multipartFile.transferTo(file);
            // 将临时文件上传到 COS
            cosManager.putObject(filePath, file);
            // 返回文件地址
            return ResultUtils.success(filePath);
        } catch (IOException e) {
            // 捕获并处理 IO 异常
            log.error("文件上传失败,filepath:{}", filePath);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 清理临时文件
            if (file != null) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败,filepath:{}", filePath);
                }

            }
        }
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filePath, HttpServletResponse response) {

        COSObjectInputStream cosObjectInputStream = null;
        try {
            // 从 COS 获取文件对象
            COSObject cosObject = cosManager.getObject(filePath);
            // 获取文件输入流
            cosObjectInputStream = cosObject.getObjectContent();

            // 设置响应头，指定内容类型为二进制流，并设置文件名
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filePath);

            // 将输入流转换为字节数组
            byte[] byteArray = IOUtils.toByteArray(cosObjectInputStream);


            // 将字节数组写入响应输出流

            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (IOException e) {
            log.error("Failed to download to response output stream,filepath:{}", filePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件下载失败");
        } finally {
            // 释放资源
            if (cosObjectInputStream != null) {
                cosObjectInputStream.release();
            }
        }
    }


}
