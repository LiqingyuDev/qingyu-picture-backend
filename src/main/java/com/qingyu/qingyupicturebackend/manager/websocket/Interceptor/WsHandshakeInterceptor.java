package com.qingyu.qingyupicturebackend.manager.websocket.Interceptor;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.qingyu.qingyupicturebackend.manager.auth.SpaceUserAuthManager;
import com.qingyu.qingyupicturebackend.manager.auth.model.SpaceUserPermissionConstants;
import com.qingyu.qingyupicturebackend.model.entity.Picture;
import com.qingyu.qingyupicturebackend.model.entity.Space;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.enums.SpaceTypeEnum;
import com.qingyu.qingyupicturebackend.service.PictureService;
import com.qingyu.qingyupicturebackend.service.SpaceService;
import com.qingyu.qingyupicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * websocket拦截器
 *
 * @author qingyu
 * @date 2025/2/25 12:37
 */
@Slf4j
@Configuration
public class WsHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private UserService userService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private SpaceService spaceService;
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 握手前校验
     *
     * @param request HTTP请求对象，包含握手请求的详细信息。
     * @param response HTTP响应对象，用于返回握手响应。
     * @param wsHandler WebSocket处理器，用于处理WebSocket连接。
     * @param attributes WebSocket会话的属性，用于存储握手过程中需要传递的数据。
     * @return 如果所有校验通过，则返回true；否则返回false。
     * @throws Exception 如果在校验过程中发生异常，则抛出。
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            // 校验登录用户
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.warn("WebSocket 握手失败：未找到登录用户");
                return false;
            }

            // 校验图片 ID
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.warn("WebSocket 握手失败：图片 ID 为空");
                return false;
            }

            // 校验图片信息
            Picture pictureById = pictureService.getById(pictureId);
            if (ObjUtil.isEmpty(pictureById)) {
                log.warn("WebSocket 握手失败：未找到图片，ID={}", pictureId);
                return false;
            }

            // 校验空间信息
            Long spaceId = pictureById.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (space == null) {
                    log.warn("WebSocket 握手失败：未找到空间，ID={}", spaceId);
                    return false;
                }
                // 非团队空间类型
                if (!space.getSpaceType().equals(SpaceTypeEnum.TEAM.getValue())) {
                    log.warn("WebSocket 握手失败：空间类型不支持，空间类型={}", space.getSpaceType());
                    return false;
                }

            }


            // 校验用户权限
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstants.PICTURE_EDIT)) {
                log.warn("WebSocket 握手失败：用户无编辑权限，用户 ID={}，空间 ID={}", loginUser.getId(), spaceId);
                return false;
            }

            // 设置 WebSocket 属性
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId));
        }

        return true;
    }

    /**
     * 暂时不实现
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Deprecated
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}