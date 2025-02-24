package com.qingyu.qingyupicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qingyu.qingyupicturebackend.exception.BusinessException;
import com.qingyu.qingyupicturebackend.exception.ErrorCode;
import com.qingyu.qingyupicturebackend.exception.ThrowUtils;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.SpaceUser;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.SpaceRoleEnum;
import com.qingyu.qingyupicturebackend.model.enums.SpaceTypeEnum;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.service.SpaceService;
import com.qingyu.qingyupicturebackend.service.SpaceUserService;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.qingyu.qingyupicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StpInterfaceImpl.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
    //服务类
    @Resource
    private SpaceService spaceService;
    @Resource
    private UserService userService;
    @Resource
    private SpaceUserService spaceUserService;
    @Autowired
    private PictureService pictureService;


    /**
     * 根据账号id获取当前账号全部权限
     *
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        //空权限列表
        List<String> permissions = new ArrayList<>();
        //校验登陆类型

        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            //类型不匹配
            return permissions;
        }
        //管理员权限
        List<String> adminPermissions = SpaceUserAuthManager.getSpaceUserAuthConfigByRole(SpaceRoleEnum.ADMIN.getValue());
        //编辑者
        List<String> editorPermissions = SpaceUserAuthManager.getSpaceUserAuthConfigByRole(SpaceRoleEnum.EDITOR.getValue());
        //浏览者
        List<String> viewerPermissions = SpaceUserAuthManager.getSpaceUserAuthConfigByRole(SpaceRoleEnum.VIEWER.getValue());

        // 管理员身份校验
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR, "用户未登录");
        Long loginUserId = loginUser.getId();
        if (userService.isAdmin(loginUser)) { // 假设 UserService 提供 isAdmin 方法
            logger.info("User {} is an admin, returning admin permissions", loginUserId);
            return adminPermissions;
        }


        // 获取上下文对象
        SpaceUserAuthContext spaceUserAuthContext = getAuthContextByRequest();
        if (isContextEmpty(spaceUserAuthContext)) {
            return adminPermissions;// 公共图库操作
        }
        SpaceUser spaceUser = spaceUserAuthContext.getSpaceUser();

        //有团队空间信息，直接返回查询到的权限
        if (spaceUser != null) {
            return SpaceUserAuthManager.getSpaceUserAuthConfigByRole(spaceUser.getSpaceRole());
        }

        // 通过 spaceUserId 获取空间用户信息
        Long spaceUserId = spaceUserAuthContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            ThrowUtils.throwIf(spaceUser == null, ErrorCode.NO_AUTH_ERROR, "用户未加入团队");
            //当前登录用户对应的 spaceUser
            LambdaQueryWrapper<SpaceUser> eq = Wrappers.lambdaQuery(SpaceUser.class)
                    .eq(SpaceUser::getUserId, loginUserId)
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId());
            SpaceUser loginSpaceUser = spaceUserService.getOne(eq);
            if (loginSpaceUser == null) {
                return permissions;
            }
            return spaceUserAuthManager.getSpaceUserAuthConfigByRole(loginSpaceUser.getSpaceRole());
        }

        //如果没有spaceUserId，可以通过spaceId或pictureId获取
        Long spaceId = spaceUserAuthContext.getSpaceId();
        if (spaceId == null) {
            Long pictureId = spaceUserAuthContext.getPictureId();
            if (pictureId == null) {
                return adminPermissions;
            }
            LambdaQueryWrapper<Picture> queryWrapper = Wrappers.lambdaQuery(Picture.class)
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getSpaceId, Picture::getId, Picture::getUserId);
            Picture pictureById = pictureService.getOne(queryWrapper);
            ThrowUtils.throwIf(pictureById == null, ErrorCode.NOT_FOUND_ERROR, "找不到图片信息");
            //图片信息不为空
            spaceId = pictureById.getSpaceId();
            //公共图库，仅仅本人和管理员可操作
            if (spaceId == null) {
                if (userService.isAdmin(loginUser) || loginUserId.equals(pictureById.getUserId())) {
                    return adminPermissions;
                }
                //仅仅可查看
                return viewerPermissions;
            }
            // spaceId不为空，通过spaceId获取空间用户信息
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "找不到空间信息");
            //通过空间类型判断
            if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
                // 私有空间，仅本人或管理员有权限
                if (space.getUserId().equals(loginUserId) || userService.isAdmin(loginUser)) {
                    return adminPermissions;
                } else {
                    return permissions;
                }
            } else if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue()) {
                // 团队空间，查询 SpaceUser 并获取角色和权限
                spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, spaceId)
                        .eq(SpaceUser::getUserId, loginUserId)
                        .one();
                if (spaceUser == null) {
                    return permissions;
                }
                return spaceUserAuthManager.getSpaceUserAuthConfigByRole(spaceUser.getSpaceRole());

            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "错误的空间类型");
            }


        }
        //spaceId不为空，通过spaceId获取空间用户信息
// 获取 Space 对象
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            // 私有空间，仅本人或管理员有权限
            if (space.getUserId().equals(loginUserId) || userService.isAdmin(loginUser)) {
                return adminPermissions;
            } else {
                return new ArrayList<>();
            }
        } else {
            // 团队空间，查询 SpaceUser 并获取角色和权限
            spaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceId)
                    .eq(SpaceUser::getUserId, loginUserId)
                    .one();
            if (spaceUser == null) {
                return new ArrayList<>();
            }
            return spaceUserAuthManager.getSpaceUserAuthConfigByRole(spaceUser.getSpaceRole());
        }
    }

    /**
     * 判断上下文是否为空
     *
     * @param context
     * @return
     */
    private boolean isContextEmpty(SpaceUserAuthContext context) {
        return context == null || (context.getSpaceId() == null && context.getSpaceUserId() == null && context.getPictureId() == null && context.getSpaceUser() == null);
    }

    /**
     * @Deprecated 实现另一个即可
     * @param loginId
     * @param loginType
     * @return
     */
    @Deprecated
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文信息并解析路径前缀
     */
    public SpaceUserAuthContext getAuthContextByRequest() {

        HttpServletRequest request = getCurrentHttpRequest().orElse(null);
        //请求的数据类型
        String contentTypeHeader = request.getHeader(Header.CONTENT_TYPE.getValue());

        SpaceUserAuthContext spaceUserAuthContext = new SpaceUserAuthContext();
        //判断是否为json请求
        if (ContentType.JSON.toString().equals(contentTypeHeader)) {
            //请求体转化为上下文对象
            String requestBody = ServletUtil.getBody(request);
            spaceUserAuthContext = JSONUtil.toBean(requestBody, SpaceUserAuthContext.class);
        } else {
            //get 请求
            spaceUserAuthContext = JSONUtil.toBean(request.getQueryString(), SpaceUserAuthContext.class);
        }

        //根据请求路径区分id
        Long id = spaceUserAuthContext.getId();
        if (id != null) {
            // 获取完整请求路径
            String requestURI = request.getRequestURI();
            // 替换掉 contextPath 部分
            String partURI = requestURI.replace(contextPath + "/", "");
// 获取前缀的第一个斜杠前的字符串(picture/space/user/spaceUser)
            String prefix = StrUtil.subBefore(partURI, "/", false);


            switch (prefix) {
                case "picture":
                    spaceUserAuthContext.setPictureId(id);
                    break;
                case "space":
                    spaceUserAuthContext.setSpaceId(id);
                    break;
                case "spaceUser":
                    spaceUserAuthContext.setSpaceUserId(id);
                    break;
                default:
            }

        }
        return spaceUserAuthContext;

    }

    /**
     * 获取当前的 HttpServletRequest 对象
     */
    private Optional<HttpServletRequest> getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return Optional.of(((ServletRequestAttributes) requestAttributes).getRequest());
        }
        return Optional.empty();
    }
}
