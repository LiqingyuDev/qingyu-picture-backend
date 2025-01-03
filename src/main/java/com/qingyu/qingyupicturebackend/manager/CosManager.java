package com.qingyu.qingyupicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qingyu.qingyupicturebackend.config.CosClientConfig;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;

/**
 * @Description: 负责与腾讯云对象存储服务 (COS) 进行交互的管理类(和业务逻辑无关)
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/17 下午8:03
 */
@Component
public class CosManager {
    /**
     * 注入配置类，用于获取 COS 的配置信息
     */
    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 注入 COS 客户端，用于执行 COS 相关的操作
     */
    @Resource
    private COSClient cosClient;

    /**
     * 将本地文件上传到 COS
     *
     * @param key  文件在 COS 中的唯一标识
     * @param file 需要上传的本地文件
     * @return 上传结果，包含上传成功后的元数据
     * @throws CosClientException  如果客户端内部发生错误
     * @throws CosServiceException 如果服务端返回错误
     */
    public PutObjectResult putObject(String key, File file) throws CosClientException, CosServiceException {
        // 创建上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 执行上传操作
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 从 COS 下载文件到本地
     *
     * @param key 文件在 COS 中的唯一标识
     * @return 包含文件内容和元数据的 COSObject 对象
     * @throws CosClientException  如果客户端内部发生错误
     * @throws CosServiceException 如果服务端返回错误
     */
    public COSObject getObject(String key) throws CosClientException, CosServiceException {
        // 创建下载请求对象，指定存储桶名称和对象键
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);

        // 执行下载操作并返回 COSObject 对象
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传并解析图片文件，同时获取图片信息。
     *
     * @param key  上传到COS的文件键（路径）
     * @param file 要上传的本地文件
     * @return 上传结果对象，包含上传状态和其他相关信息
     */
    public PutObjectResult putPictureObject(String key, File file) {
        // 创建上传请求对象
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        // 配置图片处理选项
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1); // 设置为1表示返回图片的所有信息
        //定义规则列表并添加规则
        ArrayList<PicOperations.Rule> ruleArrayList = new ArrayList<>();

        //图片压缩(转成webp格式)-- https://cloud.tencent.com/document/product/460/60524
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule webpRule = new PicOperations.Rule();
        webpRule.setRule("imageMogr2/format/webp");
        webpRule.setFileId(webpKey);
        webpRule.setBucket(cosClientConfig.getBucket());

        // 定义缩略图规则
        // 缩略图处理，仅对 > 20 KB 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            ruleArrayList.add(thumbnailRule);
        }
        ruleArrayList.add(webpRule);

        // 将规则列表添加到图片处理选项中
        picOperations.setRules(ruleArrayList);
        // 将图片处理选项添加到上传请求中
        putObjectRequest.setPicOperations(picOperations);
        // 执行上传操作并返回结果
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param url 文件 url
     */
    public void deleteObject(String url) throws CosClientException {
        String cosBucketName = cosClientConfig.getBucket();
        String host = cosClientConfig.getHost();
        String key=url.replace(host+"/", "");
        cosClient.deleteObject(cosBucketName, key);
    }


}
