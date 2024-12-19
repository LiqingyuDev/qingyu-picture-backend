package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.manager.FileManager;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author qingyu
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-18 19:31:25
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;

    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验用户是否已登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");

        // 获取请求中的图片ID
        Long pictureId = pictureUploadRequest != null ? pictureUploadRequest.getId() : null;

        // 如果ID不为空，则检查图片是否存在
        if (pictureId != null) {
            boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
        }

        // 按照用户ID划分目录，图片存在无论更新或创建都要上传
        String uploadPathPrefix = String.format("public/%d", loginUser.getId());

        // 上传图片
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);

        Picture picture = new Picture();
        // 构造实体对象
        //补充入库需要的字段
        BeanUtil.copyProperties(uploadPictureResult, picture);
        //补充入库需要的其他字段
        picture.setUserId(loginUser.getId());
        // 设置操作时间和ID
        if (pictureId != null) {
            // 更新，需要补充编辑时间和ID
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        } else {
            // 创建，设置创建时间为当前时间
            picture.setCreateTime(new Date());
        }

        // 操作数据库
        boolean saveOrUpdate = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!saveOrUpdate, ErrorCode.SYSTEM_ERROR, "图片上传失败，数据库操作失败");

        // 返回结果
        return PictureVO.objToVo(picture);
    }

}




