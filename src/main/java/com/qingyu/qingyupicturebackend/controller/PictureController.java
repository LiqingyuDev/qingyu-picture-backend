package com.qingyu.qingyupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyu.qingyupicturebackend.annotation.AuthCheck;
import com.qingyu.qingyupicturebackend.api.ImageSearchApiFacade;
import com.qingyu.qingyupicturebackend.api.model.ImageSearchResult;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.manager.CosManager;
import com.qingyu.qingyupicturebackend.manager.cache.CacheStrategy;
import com.qingyu.qingyupicturebackend.model.dto.picture.*;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.request.PictureReviewRequest;
import com.qingyu.qingyupicturebackend.model.vo.PictureTagCategory;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 图片控制器
 * @Author: liqingyu.dev@gmail.com
 * @CreateTime: 2024/12/19 下午12:49
 */
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private PictureService pictureService;
    @Resource
    private UserService userService;
    //缓存策略
    @Resource
    private CacheStrategy cacheStrategy;
    @Resource
    private CosManager cosManager;

    /**
     * 通过URL上传图片接口。
     *
     * @param pictureUploadRequest 包含图片上传所需参数的对象，特别是文件URL。
     * @param request              当前的HTTP请求对象，用于获取登录用户信息。
     * @return 返回包含上传成功图片信息的BaseResponse对象。
     */
    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "图片信息为空");
        // 校验并获取图片信息（如果提供了ID）
        if (pictureUploadRequest.getId() != null) {
            Picture existingPicture = pictureService.getById(pictureUploadRequest.getId());
            ThrowUtils.throwIf(existingPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            if (!existingPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
            }
        }

        // 上传图片
        PictureVO pictureVO = pictureService.uploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, loginUser);
        // 自动选择不同策略清除缓存
        // 在调用 clearCacheByPrefix 方法时，去掉占位符
        cacheStrategy.clearCacheByPrefix(CacheConstants.CACHE_KEY_PREFIX.replace("%s", ""));
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过文件上传图片接口。
     *
     * @param file                 待上传的图片文件。
     * @param pictureUploadRequest 包含图片上传所需参数的对象。
     * @param request              当前的HTTP请求对象，用于获取登录用户信息。
     * @return 返回包含上传成功图片信息的BaseResponse对象。
     */
    @PostMapping("/upload/file")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile file, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 校验并获取图片信息（如果提供了ID）
        if (pictureUploadRequest.getId() != null) {
            Picture existingPicture = pictureService.getById(pictureUploadRequest.getId());
            ThrowUtils.throwIf(existingPicture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            if (!existingPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
            }
        }

        // 上传图片
        PictureVO pictureVO = pictureService.uploadPicture(file, pictureUploadRequest, loginUser);
        // 自动选择不同策略清除缓存
        // 在调用 clearCacheByPrefix 方法时，去掉占位符
        cacheStrategy.clearCacheByPrefix(CacheConstants.CACHE_KEY_PREFIX.replace("%s", ""));
        return ResultUtils.success(pictureVO);
    }

    //region:增删查改

    /**
     * 根据 ID 删除图片
     *
     * @param pictureDeleteRequest 包含要删除图片的 ID 请求体
     * @param request              HTTP 请求对象，用于获取当前登录用户信息
     * @return 操作结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody PictureDeleteRequest pictureDeleteRequest, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取图片 ID
        Long id = pictureDeleteRequest.getId();
        // 调用新的 service 方法
        boolean deleteResult = pictureService.deletePicture(id, loginUser);
        return ResultUtils.success(deleteResult);
    }

    /**
     * 【管理员】更新图片信息
     *
     * @param pictureUpdateRequest 包含更新信息的请求对象
     * @param request              HTTP 请求对象，用于获取当前登录用户信息
     * @return 更新操作的结果响应
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        // 校验
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(pictureService.getById(pictureUpdateRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的图片不存在");
        // 将请求参数转换为实体对象
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);

        // 将标签列表转换为JSON字符串并设置到实体对象中
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        // 校验图片数据的有效性
        pictureService.validPicture(picture);

        // 获取图片信息
        Picture pictureById = pictureService.getById(picture.getId());

        // 填充默认审核状态
        pictureService.fillReviewParams(pictureById, userService.getLoginUser(request));

        // 更新图片信息到数据库
        boolean updatedById = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updatedById, ErrorCode.OPERATION_ERROR, "更新失败");
        // 自动选择不同策略清除缓存
        // 在调用 clearCacheByPrefix 方法时，去掉占位符
        cacheStrategy.clearCacheByPrefix(CacheConstants.CACHE_KEY_PREFIX.replace("%s", ""));
        // 返回成功响应
        return ResultUtils.success(true);
    }


    /**
     * 【用户】更新图片信息
     *
     * @param pictureEditRequest 包含更新信息的请求对象
     * @param request            HTTP 请求对象，用于获取当前登录用户信息
     * @return 更新操作的结果响应
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 校验参数
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(pictureService.getById(pictureEditRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的图片不存在");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        // 调用 service 方法
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }


    /**
     * 管理员根据 ID 获取图片（不需要脱敏）
     *
     * @param id 图片的唯一标识符
     * @return 包含图片对象的成功响应
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(@RequestParam long id, HttpServletRequest request) {
        // 校验参数是否合法
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "无效的图片 ID");

        // 查询数据库获取图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //空间权限验证
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            pictureService.validPictureAuth(userService.getLoginUser(request), picture);

        }

        // 返回查询到的图片对象
        return ResultUtils.success(picture);
    }

    /**
     * 根据 ID 获取图片（需要脱敏）
     *
     * @param id      图片的唯一标识符
     * @param request HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含脱敏后的图片对象的成功响应
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam long id, HttpServletRequest request) {
        // 校验参数是否合法
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "无效的图片 ID");

        // 查询数据库获取图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 将图片信息转换为脱敏后的视图对象 (VO)
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);

        // 返回脱敏后的图片对象
        return ResultUtils.success(pictureVO);
    }

    /**
     * 【管理员】分页获取图片列表（不需要脱敏和限制条数，不使用缓存）
     *
     * @param pictureQueryRequest 包含分页和查询条件的请求对象
     * @param request             HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含分页结果的成功响应
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 取值
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        QueryWrapper<Picture> queryWrapper = pictureService.getQueryWrapper(pictureQueryRequest);
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(picturePage);
    }


    /**
     * 分页获取图片列表（需要脱敏和限制条数）
     *
     * @param pictureQueryRequest 包含分页和查询条件的请求对象
     * @param request             HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含分页结果的成功响应
     */
    /**
     * 分页获取图片列表（需要脱敏和限制条数），使用缓存
     *
     * @param pictureQueryRequest 包含分页和查询条件的请求对象
     * @param request             HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含分页结果的成功响应
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 获取分页参数
        //int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 限制每页最多显示20条数据，防止爬虫滥用
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多显示 20 条数据");
        // 调用Service方法获取分页数据
        Page<PictureVO> pictureVOPage = pictureService.listPictureVOByPage(pictureQueryRequest, request);
        // 返回查询结果
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 标签分类
     *
     * @return 标签分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        // 初始化精简标签列表
        List<String> tagList = Arrays.asList("自然", "人物", "二次元", "美女", "城市", "科技", "艺术", "美食", "旅行", "运动", "宠物", "节日");
        // 初始化大分类列表
        List<String> categoryList = Arrays.asList("壁纸", "插画", "摄影", "图标", "海报", "头像", "表情包", "模板", "电商素材");


        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    //endregion: 增删查改

    /**
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求对象
     * @param request                     HTTP请求对象
     * @return 成功上传的图片数量
     */
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        // 自动选择不同策略清除缓存
        // 在调用 clearCacheByPrefix 方法时，去掉占位符
        cacheStrategy.clearCacheByPrefix(CacheConstants.CACHE_KEY_PREFIX.replace("%s", ""));
        return ResultUtils.success(count);
    }

    /**
     * 根据图片ID搜索相似图片。
     *
     * @param searchPictureByPictureRequest 包含图片ID的请求体
     * @return 包含相似图片搜索结果的响应对象
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        //根据id取出图片url
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        Picture pictureById = pictureService.getById(pictureId);
        ThrowUtils.throwIf(pictureById == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        String url = pictureById.getThumbnailUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "图片url为空");
        List<ImageSearchResult> imageSearchResults = ImageSearchApiFacade.searchSimilarImage(url);
        return ResultUtils.success(imageSearchResults);
    }

}