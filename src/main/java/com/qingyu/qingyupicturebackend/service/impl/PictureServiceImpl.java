package com.qingyu.qingyupicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.classmate.Annotations;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import com.qingyu.qingyupicturebackend.constant.UserConstant;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.manager.cache.CacheManager;
import com.qingyu.qingyupicturebackend.manager.cache.CacheStrategy;
import com.qingyu.qingyupicturebackend.manager.upload.FilePictureUpload;
import com.qingyu.qingyupicturebackend.manager.upload.PictureUploadTemplate;
import com.qingyu.qingyupicturebackend.manager.upload.UrlPictureUpload;
import com.qingyu.qingyupicturebackend.model.dto.file.UploadPictureResult;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureQueryRequest;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.qingyu.qingyupicturebackend.model.dto.picture.PictureUploadRequest;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.PictureReviewStatuesEnum;
import com.qingyu.qingyupicturebackend.model.request.PictureReviewRequest;
import com.qingyu.qingyupicturebackend.model.vo.PictureVO;
import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.mapper.PictureMapper;
import com.qingyu.qingyupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author qingyu
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2024-12-18 19:31:25
 */
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    //    @Resource
//    private FileManager fileManager;
    @Resource
    private PictureMapper pictureMapper;
    @Resource
    private UserService userService;
    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private FilePictureUpload filePictureUpload;


    private final CacheStrategy cacheStrategy;

    public PictureServiceImpl(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 上传图片服务方法。
     *
     * @param inputSource          图片来源对象，可以是 MultipartFile 或 URL 字符串。
     * @param pictureUploadRequest 图片上传请求对象，包含图片 ID 等信息。
     * @param loginUser            当前登录用户信息。
     * @return 图片上传成功后的视图对象（VO）。
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // MultipartFile multipartFile = (MultipartFile) inputSource;
        // 校验用户是否已登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");

        // 获取请求中的图片ID
        Long pictureId = pictureUploadRequest != null ? pictureUploadRequest.getId() : null;

        // 如果ID不为空，则检查图片是否存在
        if (pictureId != null) {
            Picture picture = this.getById(pictureId);
            //boolean exists = this.lambdaQuery().eq(Picture::getId, pictureId).exists();
            ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR, "图片不存在");
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改");
            }
        }

        // 按照用户ID划分目录，图片存在无论更新或创建都要上传
        String uploadPathPrefix = String.format("public/%d", loginUser.getId());

        // 上传图片(根据inputSource参数类型,选择不同的上传方式)

        PictureUploadTemplate pictureUploadTemplate;
        pictureUploadTemplate = null;
        if (inputSource instanceof MultipartFile) {
            pictureUploadTemplate = filePictureUpload;
        } else if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传图片类型错误");
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        Picture picture = new Picture();
        // 构造实体对象
        //补充入库需要的字段
        BeanUtil.copyProperties(uploadPictureResult, picture);
        //补充入库需要的其他字段
        picture.setUserId(loginUser.getId());
        //填充默认审核状态
        this.fillReviewParams(picture, loginUser);
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
        //审核相关
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
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
        //审核相关
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);

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
     * 分页获取图片列表并使用缓存
     *
     * @param pictureQueryRequest 查询请求对象
     * @param request             HTTP 请求对象
     * @return 分页结果
     */
    @Override
    public Page<PictureVO> listPictureVOByPage(PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        // 获取分页参数
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();

        // 设置查询条件，确保主页展示时只返回已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatuesEnum.PASS.getValue());
        QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);

        // 构建缓存key和锁key
        final String interfaceName = "listPictureVOByPage";
        String cacheKey = CacheManager.buildCacheKey(interfaceName, pictureQueryRequest);
        String lockKey = CacheManager.buildLockKey(interfaceName, pictureQueryRequest);
        // 尝试从缓存中获取数据
        String cacheValue = cacheStrategy.get(cacheKey, lockKey);
        if (StringUtils.isNotBlank(cacheValue)) {
            log.debug("Cache value for key {}: {}", cacheKey, cacheValue);
            try {
                Page<PictureVO> page = JSONUtil.toBean(cacheValue, Page.class);
                return page;
            } catch (JSONException e) {
                log.error("Failed to parse JSON from cache for key {}: {}", cacheKey, e.getMessage());
                // 清除无效缓存数据
                cacheStrategy.set(cacheKey, lockKey, null, 0, TimeUnit.SECONDS);
                // 继续执行后续逻辑
            }
        }

        // 如果缓存中没有数据，则从数据库中查询
        Page<Picture> picturePage = page(new Page<>(current, pageSize), queryWrapper);

        // 封装查询结果并转换为 VO 对象
        Page<PictureVO> pictureVOPage = getPictureVOPage(picturePage, request);
        // 将查询结果写入缓存
        final int randomExpireTime = RandomUtil.randomInt(CacheConstants.MIN_EXPIRE_TIME, CacheConstants.MAX_EXPIRE_TIME);
        cacheStrategy.set(cacheKey, lockKey, JSONUtil.toJsonStr(pictureVOPage), randomExpireTime, TimeUnit.MINUTES);
        log.debug("Set cache for key: {} with expire time: {} minutes", cacheKey, randomExpireTime);
        // 返回查询结果
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

    /**
     * 批量爬取图片
     *
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadRequest, User loginUser) {
        // 提取参数
        String searchText = pictureUploadRequest.getSearchText();
        String namePrefix = pictureUploadRequest.getNamePrefix();
        Integer count = pictureUploadRequest.getCount();
        String loginUserUserRole = loginUser.getUserRole();

        // 校验参数
        if (StrUtil.isBlank(namePrefix)) {
            pictureUploadRequest.setNamePrefix(searchText);
        }
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "抓取的关键词不能为空");
        ThrowUtils.throwIf(count == null || count < 0 || count > 20, ErrorCode.PARAMS_ERROR, "抓取的图片数量不超过20");
        ThrowUtils.throwIf(!loginUserUserRole.equals(UserConstant.ADMIN_ROLE), ErrorCode.NOT_LOGIN_ERROR, "非管理员批量爬取图片");

        // 抓取内容
        final String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s堆糖&mmasync=1", searchText);

        Connection connect = Jsoup.connect(fetchUrl);
        Document document;
        try {
            document = connect.get();
        } catch (IOException e) {
            log.error("图片抓取失败", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片抓取失败");
        }

        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imageItems = div.getElementsByClass("iusc");

        // 提取图像信息
        List<Map<String, String>> imageInfoList = new ArrayList<>();
        int uploadCount = 0;

        for (Element imageItem : imageItems) {
            Map<String, String> imageInfo = new HashMap<>();

            // 获取m属性中的JSON字符串，并解析为Map
            String mAttribute = imageItem.attr("m");
            if (StrUtil.isNotBlank(mAttribute)) {
                try {
                    Map<String, Object> mData = JSONUtil.parseObj(mAttribute);
                    imageInfo.put("title", (String) mData.get("t")); // 标题
                    imageInfo.put("mediaUrl", (String) mData.get("murl")); // 图片地址
                    imageInfo.put("sourceUrl", (String) mData.get("purl")); // 来源
                    imageInfo.put("thumbnailUrl", (String) mData.get("turl")); // 缩略图

                    // 处理图片上传地址，防止出现转义问题
                    String mediaUrl = imageInfo.get("mediaUrl");
                    if (StrUtil.isNotBlank(mediaUrl)) {
                        int questionMarkIndex = mediaUrl.indexOf("?");
                        if (questionMarkIndex > -1) {
                            mediaUrl = mediaUrl.substring(0, questionMarkIndex);
                        }

                        // 上传图片
                        UploadPictureResult uploadResult = urlPictureUpload.uploadPicture(mediaUrl, String.format("public/%d", loginUser.getId()));
                        Picture picture = new Picture();
                        BeanUtil.copyProperties(uploadResult, picture);
                        // 设置图片名称(拼接序号)
                        picture.setPicName(namePrefix + (uploadCount + 1));
                        picture.setIntroduction(imageInfo.get("title"));
                        picture.setUserId(loginUser.getId());
                        this.fillReviewParams(picture, loginUser);
                        picture.setCreateTime(new Date());

                        // 操作数据库
                        boolean saveOrUpdate = this.saveOrUpdate(picture);
                        if (saveOrUpdate) {
                            uploadCount++;
                        } else {
                            log.error("图片上传失败，数据库操作失败");
                        }
                    }

                    // 打印每个地址
                    log.info("Title: {}", imageInfo.get("title"));
                    log.info("Media URL: {}", imageInfo.get("mediaUrl"));
                    log.info("Source URL: {}", imageInfo.get("sourceUrl"));
                    log.info("Thumbnail URL: {}", imageInfo.get("thumbnailUrl"));
                    log.info("----------------------------------------");

                } catch (Exception e) {
                    log.error("解析图片信息失败: {}", e);
                }
            }

            // 达到指定数量后退出循环
            if (uploadCount >= count) {
                break;
            }
        }

        return uploadCount;
    }

    /**
     * 审核图片，更新图片审核状态。
     *
     * @param pictureReviewRequest 图片审核请求参数，包含审核意见和状态
     * @param loginUser            当前登录用户信息
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        //取出参数
        Long pictureId = pictureReviewRequest.getId();
        Integer newReviewStatus = pictureReviewRequest.getReviewStatus();
        String newReviewMessage = pictureReviewRequest.getReviewMessage();
        Long loginUserId = loginUser.getId();
        //判断图片是否存在
        Picture oldPictureById = this.getById(pictureId);
        ThrowUtils.throwIf(oldPictureById == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        //判断是否是管理员
        ThrowUtils.throwIf(!loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE), ErrorCode.NO_AUTH_ERROR, "无权限审核");
        //判断图片当前审核状态
        Integer oldReviewStatus = oldPictureById.getReviewStatus();
        ThrowUtils.throwIf(Objects.equals(newReviewStatus, oldReviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        //更新图片审核状态
        Picture picture = new Picture();
        picture.setId(pictureId);
        picture.setReviewStatus(newReviewStatus);
        picture.setReviewMessage(newReviewMessage);
        picture.setReviewerId(loginUserId);//审核人id
        picture.setReviewTime(new Date());
        ThrowUtils.throwIf(!this.updateById(picture), ErrorCode.OPERATION_ERROR, "审核失败");
    }

    /**
     * 填充默认审核状态
     *
     * @param picture
     * @param loginUser
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        //如果是管理员,则默认审核通过
        if (userService.isAdmin(loginUser)) {
            picture.setReviewStatus(PictureReviewStatuesEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动审核通过");
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());

        } else {
            picture.setReviewStatus(PictureReviewStatuesEnum.REVIEWING.getValue());
            picture.setReviewMessage("等待管理员审核中");
            picture.setReviewerId(null);
            picture.setReviewTime(null);
        }

    }
}




