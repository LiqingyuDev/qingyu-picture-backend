package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.manager.FileManager;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureQueryRequest;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.mapper.PictureMapper;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qingyu
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-18 19:31:25
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {
    @Resource
    private FileManager fileManager;
    @Resource
    private PictureMapper pictureMapper;
    @Resource
    private UserService userService;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
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

    /**
     * 获取mybatis-plus查询条件
     *
     * @param pictureQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        ThrowUtils.throwIf(pictureQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        //从请求参数中获取查询条件
        Long id = pictureQueryRequest.getId();
        String picName = pictureQueryRequest.getPicName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        Long userId = pictureQueryRequest.getUserId();
        String searchText = pictureQueryRequest.getSearchText();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        //创建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        //从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 模糊搜索拼接多个查询条件
            queryWrapper.like("picName", searchText).or().like("introduction", searchText);
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(picName), "picName", picName);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        // Json数组字段查询
        // 遍历 tags 数组，每个标签都使用 like 模糊查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        //排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 为picture封装并关联用户信息(为pictureVO关联userVO)
     *
     * @param picture
     * @return
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }

        return pictureVO;
    }

    /**
     * 分页获取封装后的分页数据(为pictureVO关联userVO)
     *
     * @param picturePage
     * @param request
     * @return
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 -> 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 查询关联用户id集合
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userListByUserId = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));

        // 为每个 PictureVO 设置关联的 UserVO
        for (PictureVO pictureVO : pictureVOList) {
            Long userId = pictureVO.getUserId();
            if (userId != null && userListByUserId.containsKey(userId)) {
                User user = userListByUserId.get(userId).get(0); // 假设每个 userId 只对应一个 User
                UserVO userVO = userService.getUserVO(user);
                pictureVO.setUser(userVO);
            }
        }

        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 校验图片对象的有效性
     *
     * @param picture 需要校验的图片对象
     * @throws BusinessException 如果图片对象无效，则抛出业务异常
     */
    @Override
    public void validPicture(Picture picture) {
        // 检查图片对象是否为空
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片对象不能为空");

        // 提取图片对象中的属性
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        // 在修改数据时，ID 必须存在且有效
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "ID 不能为空");

        // 如果 URL 不为空，则检查其长度是否超过限制
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "URL 长度不能超过 1024 个字符");
        }

        // 如果简介不为空，则检查其长度是否超过限制
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介长度不能超过 800 个字符");
        }
    }


}




