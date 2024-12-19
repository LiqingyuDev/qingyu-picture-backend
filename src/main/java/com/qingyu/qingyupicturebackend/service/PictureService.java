package com.qingyu.qingyupicturebackend.service;

import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author qingyu
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-18 19:31:25
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);
}
