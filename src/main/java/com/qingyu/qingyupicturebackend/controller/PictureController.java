package com.qingyu.qingyupicturebackend.controller;

import com.qingyu.qingyupicturebackend.annotation.AuthCheck;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.qingyu.qingyupicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @Description: 图片控制器
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/19 下午12:49
 */
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;

    /**
     * 上传图片（仅限管理员使用）
     * <p>
     * 该接口用于管理员上传图片，并返回上传成功的图片信息。
     *
     * @param file                 待上传的图片文件
     * @param pictureUploadRequest 包含图片上传所需参数的对象
     * @param request              当前的HTTP请求对象，用于获取登录用户信息
     * @return 返回包含上传成功图片信息的BaseResponse对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/picture/upload")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "上传文件为空");
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "上传参数为空");
        // 调用图片上传服务
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
    //region:增删查改

    //endregion: 增删查改


}