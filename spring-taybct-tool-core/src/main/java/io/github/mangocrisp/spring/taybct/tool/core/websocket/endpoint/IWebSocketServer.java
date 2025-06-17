package io.github.mangocrisp.spring.taybct.tool.core.websocket.endpoint;

import cn.hutool.core.util.IdUtil;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.enums.MessageUserType;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.MessageUser;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebSocketMessagePayload;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <pre>
 * websocket 服务
 * </pre>
 *
 * @param <S> session 类型
 * @author xijieyin
 * @since 2025/6/13 11:17
 */
public interface IWebSocketServer<S> {

    /**
     * 查询所有用户关联的会话
     *
     * @param userId 用户 id
     * @return session
     */
    CopyOnWriteArraySet<S> getSession(Long userId);

    /**
     * 查询 session
     *
     * @param sessionId 会话 id
     * @return 会话
     */
    S getSession(String sessionId);

    /**
     * 获取会话的请求参数
     *
     * @param sessionId 会话 id
     * @return 请求参数
     */
    Map<String, List<String>> getRequestParameterMap(String sessionId);

    /**
     * 获取会话的路径参数
     * @param sessionId 会话 id
     * @return 路径参数
     */
    Map<String, String> getPathParameterMap(String sessionId);

    /**
     * 缓存 session
     *
     * @param userId  用户 id
     * @param session session
     */
    void cacheSession(Long userId, S session);

    /**
     * 打开链接
     *
     * @param session 会话
     * @param userId  用户 id
     */
    default void onOpen(Long userId, S session) {
        // 连接成功之后的操作
        onlineCount();
    }

    /**
     * 状态处理
     *
     * @param session 会话
     * @param userId  用户 id
     * @param code    状态码
     * @param reason  原因
     */
    default void onStatus(S session, Long userId, int code, String reason) {
        // 仅 reactive 下可用，用于处理各种不同状态码时该做的操作
    }

    /**
     * 关闭链接
     *
     * @param session 会话
     * @param userId  用户 id
     */
    default void onClose(S session, Long userId) {
        // 关闭连接之后的操作
    }

    /**
     * 出现异常
     *
     * @param session 会话
     * @param userId  用户 id
     * @param error   异常
     */
    default void onError(S session, Long userId, Throwable error) {
        // 出现异常的处理
    }

    /**
     * 在线统计
     */
    default long onlineCount() {
        // 统计在线数量
        return 0L;
    }

    /**
     * 接收消息
     *
     * @param userId  用户 id
     * @param message 消息
     */
    default void onMessage(S session, Long userId, String message) {
        // 接收消息
    }

    /**
     * byte 数据
     *
     * @param userId        用户 id
     * @param binaryMessage 二进制数据
     */
    default void onMessage(S session, Long userId, byte[] binaryMessage) {
        // 接收二进制消息
    }

    /**
     * 发送简单文本条消息 (匿名)
     *
     * @param message  消息
     * @param toUserId 要发送给的用户 id
     */
    default void sendSimpleMessage(String message, Long... toUserId) {
        sendSimpleMessage(message, new LinkedHashSet<>(Arrays.stream(toUserId).map(id -> new MessageUser(MessageUserType.USER, id, null)).toList()));
    }

    /**
     * 发送简单文本条消息 (匿名)
     *
     * @param fromUserId 发送消息的用户 id
     * @param message    消息
     * @param toUserId   要发送给的用户 id
     */
    default void sendSimpleMessage(Long fromUserId, String message, Long... toUserId) {
        sendSimpleMessage(new MessageUser(MessageUserType.USER, fromUserId, null)
                , message, new LinkedHashSet<>(Arrays.stream(toUserId).map(id -> new MessageUser(MessageUserType.USER, id, null)).toList()));
    }

    /**
     * 发送简单文本条消息 (匿名)
     *
     * @param message 消息
     * @param toUser  要发送给的用户
     */
    default void sendSimpleMessage(String message, MessageUser... toUser) {
        sendSimpleMessage(null, message, toUser);
    }

    /**
     * 发送简单文本条消息
     *
     * @param fromUser 发送消息的用户
     * @param message  消息
     * @param toUser   要发送给的用户
     */
    default void sendSimpleMessage(@Nullable MessageUser fromUser, String message, MessageUser... toUser) {
        sendSimpleMessage(fromUser, message, new LinkedHashSet<>(Arrays.asList(toUser)));
    }

    /**
     * 发送简单文本条消息
     *
     * @param message   消息
     * @param toUserSet 要发送给的用户集合
     */
    default void sendSimpleMessage(String message, LinkedHashSet<MessageUser> toUserSet) {
        sendSimpleMessage(null, message, toUserSet);
    }

    /**
     * 发送简单文本条消息
     *
     * @param fromUser  发送消息的用户
     * @param message   消息
     * @param toUserSet 要发送给的用户集合
     */
    default void sendSimpleMessage(@Nullable MessageUser fromUser, String message, LinkedHashSet<MessageUser> toUserSet) {
        // 发送消息
        sendMessage(WebSocketMessagePayload.builder()
                .messageId(IdUtil.randomUUID())
                .sendTime(LocalDateTime.now())
                .fromUser(fromUser)
                .toUser(toUserSet)
                .content(message)
                .build());
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    default void sendMessage(WebSocketMessagePayload message) {

    }

    /**
     * 广播发给所有在线的人
     *
     * @param message 消息
     */
    default void sendAllMessage(WebSocketMessagePayload message) {
        // 发送消息
    }

    /**
     * 发送消息
     *
     * @param message 消息
     * @param session session
     */
    void send(WebSocketMessagePayload message, S session);
}
