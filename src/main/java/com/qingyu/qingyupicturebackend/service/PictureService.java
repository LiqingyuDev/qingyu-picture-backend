package com.qingyu.qingyupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureQueryRequest;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.request.PictureReviewRequest;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author qingyu
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2024-12-18 19:31:25
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片服务方法。
     *
     * @param inputSource          图片来源对象，可以是 MultipartFile 或 URL 字符串。
     * @param pictureUploadRequest 图片上传请求对象，包含图片 ID 等信息。
     * @param loginUser            当前登录用户信息。
     * @return 图片上传成功后的视图对象（VO）。
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 根据查询请求构建查询条件。
     *
     * @param pictureQueryRequest 图片查询请求参数，包含查询条件如标题、标签等
     * @return 返回封装了查询条件的 QueryWrapper 对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 将数据库中的图片实体转换为视图对象。
     *
     * @param picture 数据库中的图片实体
     * @param request HTTP 请求对象，用于获取上下文信息
     * @return 返回图片视图对象（PictureVO）
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页查询图片，并将结果转换为视图对象列表。
     *
     * @param picturePage 分页查询结果
     * @param request     HTTP 请求对象，用于获取上下文信息
     * @return 返回分页后的图片视图对象列表
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 审核图片，更新图片审核状态。
     *
     * @param pictureReviewRequest 图片审核请求参数，包含审核意见和状态
     * @param loginUser            当前登录用户信息
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 分页获取图片列表并使用缓存
     *
     * @param pictureQueryRequest 查询请求对象
     * @param request             HTTP 请求对象
     * @return 分页结果
     */
    Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request);

    /**
     * 验证图片信息是否合法。
     *
     * @param picture 待验证的图片实体
     */
    void validPicture(Picture picture);

    /**
     * 批量爬取图片
     *
     * @param pictureUpload
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadRequest, User loginUser);
}
