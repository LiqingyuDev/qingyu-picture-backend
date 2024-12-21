package com.qingyu.qingyupicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyu.qingyupicturebackend.annotation.AuthCheck;
import com.qingyu.qingyupicturebackend.common.BaseResponse;
import com.qingyu.qingyupicturebackend.common.ResultUtils;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.dto.picture.*;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.PictureTagCategory;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    @PostMapping("/upload")
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

    /**
     * 根据 ID 删除图片
     *
     * @param pictureDeleteRequest 包含要删除图片的 ID 请求体
     * @param request              HTTP 请求对象，用于获取当前登录用户信息
     * @return 操作结果
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody PictureDeleteRequest pictureDeleteRequest, HttpServletRequest request) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(pictureDeleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 获取图片 ID
        Long id = pictureDeleteRequest.getId();
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "图片 ID 不能为空");

        // 获取图片创建者 ID
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        Long builderId = picture.getUserId();

        // 如果不是创建者或管理员，直接返回无权限错误
        if (!(loginUser.getId().equals(builderId) || loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除");
        }

        // 执行删除操作
        boolean deleteResult = pictureService.removeById(id);
        ThrowUtils.throwIf(!deleteResult, ErrorCode.OPERATION_ERROR, "删除失败");

        return ResultUtils.success(true);
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

        // 校验参数
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(pictureService.getById(pictureUpdateRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的图片不存在");
        //dto转换entity
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        //list转为json字符串
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        //图片数据校验
        pictureService.validPicture(picture);
        //操作数据库
        boolean updatedById = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updatedById, ErrorCode.OPERATION_ERROR, "更新失败");
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

        // 校验参数
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(BeanUtil.isEmpty(pictureService.getById(pictureEditRequest.getId())), ErrorCode.PARAMS_ERROR, "修改的图片不存在");
        //dto转换entity
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        //list转为json字符串
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        //更新编辑时间
        picture.setEditTime(new Date());
        //图片数据校验
        pictureService.validPicture(picture);
        //仅本人或管理员可以修改
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        if (!loginUserId.equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
        }
        //操作数据库
        boolean updatedById = pictureService.updateById(picture);
        ThrowUtils.throwIf(!updatedById, ErrorCode.OPERATION_ERROR, "更新失败");
        return ResultUtils.success(true);
    }


    /**
     * 管理员根据 ID 获取图片（不需要脱敏）
     *
     * @param id      图片的唯一标识符
     * @param request HTTP 请求对象，用于获取当前登录用户信息
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
     * 分页获取图片列表（需要脱敏和限制条数）
     *
     * @param pictureQueryRequest 包含分页和查询条件的请求对象
     * @param request             HTTP 请求对象，用于获取当前登录用户信息
     * @return 包含分页结果的成功响应
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        // 取值
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫：每页最多显示 20 条数据
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多显示 20 条数据");
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        // 封装返回结果
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        return ResultUtils.success(pictureVOPage);
    }

    /**
     * 【管理员】分页获取图片列表（不需要脱敏和限制条数）
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
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 标签分类
     *
     * @return 标签分类
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }
    //endregion: 增删查改


}