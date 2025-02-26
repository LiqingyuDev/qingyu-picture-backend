package com.qingyu.qingyupicturebackend.manager.websocket;

import com.qingyu.qingyupicturebackend.manager.websocket.Handler.PictureEditHandler;
import com.qingyu.qingyupicturebackend.manager.websocket.Interceptor.WsHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.Resource;

/**
 * WebSocketConfig 类用于配置 WebSocket 相关设置。
 * 该类通过 Spring 的 WebSocket 支持，定义 WebSocket 的处理器、拦截器以及允许的跨域来源。
 *
 * <p>主要功能包括：
 * <ul>
 *     <li>注册 WebSocket 处理器，处理图片编辑相关的 WebSocket 连接和消息。</li>
 *     <li>添加 WebSocket 握手拦截器，用于在握手阶段进行身份验证或其他逻辑处理。</li>
 *     <li>配置允许的跨域来源，支持所有来源的 WebSocket 连接。</li>
 * </ul>
 *
 * @author qingyu
 * @date 2025/2/26 15:05
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    private PictureEditHandler pictureEditHandler;
    @Resource
    private WsHandshakeInterceptor wsHandshakeInterceptor;

    /**
     * 注册 WebSocket 处理器和拦截器。
     * 配置 WebSocket 的路径、拦截器以及允许的跨域来源。
     *
     * @param registry WebSocket 处理器注册器，用于注册处理器和拦截器
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandlerRegistration webSocketHandlerRegistration = registry
                .addHandler(pictureEditHandler, "/ws/picture/edit")
                .addInterceptors(wsHandshakeInterceptor)
                .setAllowedOrigins("*");

    }
}
