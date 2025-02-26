package com.qingyu.qingyupicturebackend.manager.websocket.Handler;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.qingyu.qingyupicturebackend.manager.websocket.model.PictureEditRequestMessage;
import com.qingyu.qingyupicturebackend.manager.websocket.model.PictureEditResponseMessage;
import com.qingyu.qingyupicturebackend.manager.websocket.model.enums.PictureEditActionEnum;
import com.qingyu.qingyupicturebackend.manager.websocket.model.enums.PictureEditMessageEnum;
import com.qingyu.qingyupicturebackend.model.entity.User;
import com.qingyu.qingyupicturebackend.model.vo.UserVO;
import com.qingyu.qingyupicturebackend.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PictureEditHandler 类负责处理与图片编辑相关的 WebSocket 连接和消息。
 * 该类通过 WebSocket 实现图片编辑的实时协作功能，允许用户编辑图片并实时同步编辑状态给其他用户。
 *
 * <p>主要功能包括：
 * <ul>
 *     <li>管理每张图片的编辑状态，确保同一时间只有一位用户可以编辑图片。</li>
 *     <li>维护所有连接的 WebSocket 会话，以便在图片编辑过程中广播编辑状态给所有相关用户。</li>
 * </ul>
 *
 * @author qingyu
 * @date 2025/2/25 17:13
 */
@Component
public class PictureEditHandler extends TextWebSocketHandler {

    // 每张图片的编辑状态，key: 图片ID (pictureId), value: 当前正在编辑的唯一一位用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的 WebSocket 会话，key: 图片ID (pictureId), value: 所有能观测编辑过程的用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();
    // 用于将 Java 对象转换为 JSON 字符串的 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    public PictureEditHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * 当 WebSocket 连接成功建立时调用此方法。
     * 保存会话到集合中，并且给其他会话发送消息：
     *
     * @param session 新建立的 WebSocket 会话
     * @throws Exception 如果处理过程中发生错误
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User loginUser = (User) session.getAttributes().get("user");
        // 如果集合中不存在该图片的记录，则创建一个空的集合
        pictureSessions.putIfAbsent(pictureId, ConcurrentHashMap.newKeySet());
        boolean add = pictureSessions.get(pictureId).add(session);

        //给其他用户发送消息
        //1. 构造响应类
        PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
        responseMessage.setType(PictureEditMessageEnum.INFO.getValue());
        responseMessage.setUser(UserVO.objToVo(loginUser));
        String message = String.format("用户 %s 进入编辑", loginUser.getUserName());
        responseMessage.setMessage(message);
        //2. 广播消息
        broadcastMessage(pictureId, responseMessage);

    }

    /**
     * 当接收到来自客户端的文本消息时调用此方法。
     *
     * @param session 发送消息的 WebSocket 会话
     * @param message 接收到的文本消息
     * @throws Exception 如果处理过程中发生错误
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        //根据消息类型不同采取不同的处理策略
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageEnum typeEnumByValue = PictureEditMessageEnum.getEnumByValue(type);


        Long pictureId = (Long) session.getAttributes().get("pictureId");
        User loginUser = (User) session.getAttributes().get("user");

        switch (typeEnumByValue) {
            case ENTER_EDIT:
                // 进入编辑
                handleEnterEdit(session, pictureEditRequestMessage, pictureId, loginUser);
                break;
            case EDIT_ACTION:
                // 编辑操作
                handleEditAction(session, pictureEditRequestMessage, pictureId, loginUser);
                break;
            case EXIT_EDIT:
                // 退出编辑
                handleExitEdit(session, pictureEditRequestMessage, pictureId, loginUser);
                break;
            default:
                //错误
                PictureEditResponseMessage responseMessage = new PictureEditResponseMessage();
                responseMessage.setType(PictureEditMessageEnum.ERROR.getValue());
                responseMessage.setMessage("未知错误");
                responseMessage.setUser(UserVO.objToVo(loginUser));
                session.sendMessage(new TextMessage(JSONUtil.toJsonStr(responseMessage)));
                break;
        }
    }
    // 退出编辑

    private void handleExitEdit(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, Long pictureId, User loginUser) throws Exception {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(loginUser.getId())) {
            // 移除当前用户的编辑状态
            pictureEditingUsers.remove(pictureId);
            // 构造响应，发送退出编辑的消息通知
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageEnum.EXIT_EDIT.getValue());
            String message = String.format("%s退出编辑图片", loginUser.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(userService.getUserVO(loginUser));
            broadcastMessage(pictureId, pictureEditResponseMessage);
        }

    }
    // 编辑操作

    private void handleEditAction(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, Long pictureId, User loginUser) throws Exception {

        String editAction = pictureEditRequestMessage.getEditAction();
        //
        Long userId = pictureEditingUsers.get(pictureId);
        Long loginUserId = loginUser.getId();
        if (loginUserId == userId) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageEnum.EDIT_ACTION.getValue());
            String message = String.format("用户 %s执行%s中", loginUser.getUserName(), Objects.requireNonNull(PictureEditActionEnum.getEnumByValue(editAction)).getText());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setEditAction(editAction);
            pictureEditResponseMessage.setUser(UserVO.objToVo(loginUser));

            // 广播给除了当前客户端之外的其他用户，否则会造成重复编辑
            broadcastMessage(pictureId, pictureEditResponseMessage, session);

        }

    }

    /**
     * 处理用户进入图片编辑的逻辑。
     * 如果当前图片未被其他用户编辑，则将当前用户设置为编辑者，并向所有协作者广播用户进入编辑的消息。
     *
     * @param session   当前用户的 WebSocket 会话
     * @param pictureId 当前编辑的图片 ID
     * @param loginUser 当前登录用户对象
     * @throws Exception 如果在广播消息过程中发生错误
     */
    private void handleEnterEdit(WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage, Long pictureId, User loginUser) throws Exception {
        // 检查当前图片是否已被其他用户编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            // 将当前用户设置为图片的编辑者
            pictureEditingUsers.put(pictureId, loginUser.getId());
            pictureEditResponseMessage.setType(PictureEditMessageEnum.ENTER_EDIT.getValue());
            String message = String.format("用户 %s 进入编辑", loginUser.getUserName());
            pictureEditResponseMessage.setMessage(message);
            pictureEditResponseMessage.setUser(UserVO.objToVo(loginUser));
            // 向所有协作者广播用户进入编辑的消息
            broadcastMessage(pictureId, pictureEditResponseMessage);
        }
    }

    /**
     * 当 WebSocket 连接关闭时调用此方法。
     *
     * @param session 已关闭的 WebSocket 会话
     * @param status  连接关闭的状态
     * @throws Exception 如果处理过程中发生错误
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Map<String, Object> attributes = session.getAttributes();
        Long pictureId = (Long) attributes.get("pictureId");
        User user = (User) attributes.get("user");
        // 移除当前用户的编辑状态
        handleExitEdit(session, null, pictureId, user);

        // 删除会话
        Set<WebSocketSession> sessionSet = pictureSessions.get(pictureId);
        if (sessionSet != null) {
            sessionSet.remove(session);
            if (sessionSet.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }

        // 响应
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageEnum.INFO.getValue());
        String message = String.format("%s离开编辑", user.getUserName());
        pictureEditResponseMessage.setMessage(message);
        pictureEditResponseMessage.setUser(userService.getUserVO(user));
        broadcastMessage(pictureId, pictureEditResponseMessage);
    }


    /**
     * 向指定图片的所有协作者广播消息(排除某人)。
     *
     * @param pictureId                  图片ID，用于确定需要广播的会话
     * @param pictureEditResponseMessage 需要发送的图片编辑响应消息
     * @param excludeSession             需要排除的会话（可选，如果不需要排除传 null）
     * @throws Exception 如果发送消息过程中发生错误
     */

    private void broadcastMessage(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage, WebSocketSession excludeSession) throws Exception {
        // 获取与指定图片相关的所有会话
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (sessions == null || sessions.isEmpty()) {
            return; // 如果没有相关会话，直接返回
        }

        // 创建 ObjectMapper 并配置序列化规则
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);

        // 序列化为 JSON 字符串
        String jsonMessage = objectMapper.writeValueAsString(pictureEditResponseMessage);
        TextMessage message = new TextMessage(jsonMessage);

        // 遍历所有会话，发送消息
        for (WebSocketSession session : sessions) {
            // 排除指定的会话
            if (excludeSession != null && session.getId().equals(excludeSession.getId())) {
                continue;
            }

            // 检查会话是否仍然打开
            if (session.isOpen()) {
                session.sendMessage(message);
            }
        }
    }


    /**
     * 向指定图片的所有协作者广播消息。
     *
     * @param pictureId                  图片ID，用于确定需要广播的会话
     * @param pictureEditResponseMessage 需要发送的图片编辑响应消息
     * @throws Exception 如果发送消息过程中发生错误
     */

    private void broadcastMessage(Long pictureId, PictureEditResponseMessage pictureEditResponseMessage) throws Exception {
        broadcastMessage(pictureId, pictureEditResponseMessage, null);
    }

}
