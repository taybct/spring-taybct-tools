package io.github.mangocrisp.spring.taybct.tool.core.websocket.endpoint;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.constant.MessageTopic;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.enums.MessageUserType;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.MessageUser;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WSR;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <pre>
 * 接口路径 ws://localhost:8400/webSocket/userId;
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/3/14 15:37
 */
@Getter
@Slf4j
@RequiredArgsConstructor
@ConditionalOnClass(Session.class)
public abstract class AbstractWebSocketServer implements IWebSocketServer<Session> {

    /**
     * 存放 session
     */
    protected static final ConcurrentHashMap<Long, CopyOnWriteArraySet<Session>> userSessionPool = new ConcurrentHashMap<>();
    /**
     * 存放 websocket 会话
     */
    protected static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();
    /**
     * 存放请求参数
     */
    protected static final ConcurrentHashMap<String, Map<String, List<String>>> requestParameterMap = new ConcurrentHashMap<>();
    /**
     * 存放路径请求参数
     */
    protected static final ConcurrentHashMap<String, Map<String, String>> pathParameterMap = new ConcurrentHashMap<>();
    /**
     * 会话用户
     */
    protected static final ConcurrentHashMap<String, Long> sessionUser = new ConcurrentHashMap<>();

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        requestParameterMap.put(session.getId(), session.getRequestParameterMap());
        pathParameterMap.put(session.getId(), session.getPathParameters());
        sessionUser.put(session.getId(), userId);
        cacheSession(userId, session);
        onOpen(userId, session);
    }

    @Override
    public void onOpen(Long userId, Session session) {
        log.debug("用户已连接：{}:{}", userId, session.getId());
        onlineCount();
        // 返回告诉客户端连接成功，返回的第一条消息就是 sessionId
        sendMessage(WSR.ok("连接成功！")
                .setData(JSONObject.of("sessionId", session.getId(), "userId", userId))
                .setToUser(new LinkedHashSet<>(Collections.singletonList(new MessageUser(MessageUserType.USER, userId, session.getId()))))
                .setTopic(MessageTopic.SYSTEM_MESSAGE));
    }

    @Override
    public void cacheSession(Long userId, Session session) {
        CopyOnWriteArraySet<Session> userWebSocketRecordPoolOrDefault = userSessionPool.getOrDefault(userId, new CopyOnWriteArraySet<>());
        userWebSocketRecordPoolOrDefault.add(session);
        userSessionPool.put(userId, userWebSocketRecordPoolOrDefault);
        sessionPool.put(session.getId(), session);
    }

    @Override
    public CopyOnWriteArraySet<Session> getSession(Long userId) {
        return userSessionPool.get(userId);
    }

    @Override
    public Session getSession(String sessionId) {
        return sessionPool.get(sessionId);
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap(String sessionId) {
        return requestParameterMap.get(sessionId);
    }

    @Override
    public Map<String, String> getPathParameterMap(String sessionId) {
        return pathParameterMap.get(sessionId);
    }

    /**
     * 发送错误时的处理
     */
    @OnError
    public void onError(Session session, Throwable error) {
        onError(session, sessionUser.get(session.getId()), error);
    }

    @Override
    public void onError(Session session, Long userId, Throwable error) {
        log.error("WebSocket 异常！{}", userId, error);
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        Long userId = sessionUser.get(session.getId());
        log.debug("用户[{}:{}]已断开连接！", userId, session.getId());// 当连接关闭时执行，无论正常关闭还是异常关闭
        // 删除用户和 session 的关联
        userSessionPool.getOrDefault(userId, new CopyOnWriteArraySet<>()).remove(session);
        // 删除 session
        sessionPool.remove(session.getId());
        // 删除session的请求参数
        requestParameterMap.remove(session.getId());
        pathParameterMap.remove(session.getId());
        sessionUser.remove(session.getId());
        onClose(session, userId);
        onlineCount();
    }

    @Override
    public long onlineCount() {
        log.debug("连接总数为: {}", sessionPool.size());
        return sessionPool.size();
    }

    /**
     * 接收文字数据
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        onMessage(session, sessionUser.get(session.getId()), message);
    }

    /**
     * 收到客户端消息后调用的方法(二进制)
     */
    @OnMessage
    public void onMessage(Session session, byte[] message) {
        onMessage(session, sessionUser.get(session.getId()), message);
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public <E> void sendMessage(WSR<E> message) {
        if (!ObjectUtils.isEmpty(message)) {
            log.debug("【websocket消息】 消息:{}", JSONObject.toJSONString(message));
            if (CollectionUtil.isEmpty(message.getToUser())) {
                sendAllMessage(message);
            } else {
                message.getToUser().forEach(messageToUser -> {
                    if (StringUtils.hasText(messageToUser.sessionId()) && sessionPool.containsKey(messageToUser.sessionId())) {
                        // 如果设置了指定的 session id 则发送给指定的 session
                        sendMessage(message, messageToUser, sessionPool.get(messageToUser.sessionId()));
                    } else {
                        // 如果只是指定发给某个用户，那这个用户所有联接的会话都会收到消息
                        if (userSessionPool.containsKey(messageToUser.userId())) {
                            userSessionPool.get(messageToUser.userId()).forEach(session -> sendMessage(message, messageToUser, session));
                        } else {
                            log.warn("用户{}的websocket连接不存在或已断开！", messageToUser.userId());
                        }
                    }
                });
            }
        }
    }

    public <E> void sendMessage(WSR<E> message, MessageUser messageToUser, Session session) {
        if (session.isOpen()) {
            try {
                send(message, session);
            } catch (Exception e) {
                log.error("消息发送失败！", e);
                afterSend(message, e);
            }
        } else {
            log.warn("用户{}:{}的websocket连接不存在或已断开！", messageToUser.userId(), session.getId());
        }
    }

    /**
     * 发送广播消息
     *
     * @param message 消息
     */
    @Override
    public <E> void sendAllMessage(WSR<E> message) {
        userSessionPool.values().stream().flatMap(Collection::stream)
                .forEach(session -> {
                    try {
                        if (session.isOpen()) {
                            send(message, session);
                        }
                    } catch (Exception e) {
                        log.error("广播消息失败！", e);
                        afterSend(message, e);
                    }
                });
    }

    /**
     * 发送
     *
     * @param message 消息
     * @param session session
     */
    public <E> void send(WSR<E> message, Session session) {
        if (message.getBytes() != null) {
            session.getAsyncRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
        } else {
            session.getAsyncRemote().sendText(JSONObject.toJSONString(message));
        }
        afterSend(message, null);
    }

    @Override
    public <E> void afterSend(WSR<E> message, @Nullable Throwable error) {
        if (error != null) {
            log.error("发送失败！{}", JSONObject.toJSONString(message), error);
            onSendError(message, error);
        } else {
            onSendSuccess(message);
        }
        if (ObjectUtil.isNotNull(message.getFromUser())) {
            MessageUser fromUser = message.getFromUser();
            if (StringUtils.hasText(fromUser.sessionId()) && sessionPool.containsKey(fromUser.sessionId())) {
                afterSend(message, error, sessionPool.get(fromUser.sessionId()));
            } else {
                if (userSessionPool.containsKey(fromUser.userId())) {
                    try {
                        userSessionPool.get(fromUser.userId()).forEach(session -> afterSend(message, error, session));
                    } catch (Exception e) {
                        log.error("发送失败消息失败！", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public <E> void afterSend(WSR<E> message, Throwable error, Session session) {
        session.getAsyncRemote()
                .sendText(JSONObject.toJSONString((error == null ? WSR.ok("发送成功！") : WSR.fail("发送失败！"))
                        .setData(JSONObject.of("source", JSONObject.toJSONString(message)))
                        .setTopic(MessageTopic.SYSTEM_MESSAGE)
                        .setError(error)));
    }
}
