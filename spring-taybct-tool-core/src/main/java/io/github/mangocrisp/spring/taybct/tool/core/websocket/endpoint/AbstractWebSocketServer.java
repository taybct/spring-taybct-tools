package io.github.mangocrisp.spring.taybct.tool.core.websocket.endpoint;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.MessageUser;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebSocketMessagePayload;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashSet;
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
@Slf4j
@RequiredArgsConstructor
@ConditionalOnClass(Session.class)
public abstract class AbstractWebSocketServer implements IWebSocketServer<Session> {

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * <pre>
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     * 虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean，
     * 所以可以用一个静态set保存起来。
     * 注：底下WebSocket是当前类名
     * </pre>
     */
    private static final CopyOnWriteArraySet<AbstractWebSocketServer> webSockets = new CopyOnWriteArraySet<>();

    /**
     * <pre>
     * 用来存在线连接用户信息
     * </pre>
     */
    private static final ConcurrentHashMap<Long, Session> sessionPool = new ConcurrentHashMap<>();

    @Override
    public Session getSession(Long userId) {
        return sessionPool.get(userId);
    }

    /**
     * 链接成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "userId") Long userId) {
        this.session = session;
        this.userId = userId;
        webSockets.add(this);
        sessionPool.put(userId, session);
        onOpen(userId);
    }

    @Override
    public void onOpen(Long userId) {
        log.debug("用户已连接：{}", userId);
        onlineCount();
    }

    /**
     * 发送错误时的处理
     */
    @OnError
    public void onError(Throwable error) {
        onError(userId, error);
    }

    @Override
    public void onError(Long userId, Throwable error) {
        log.error("WebSocket 异常！{}", userId, error);
    }

    /**
     * 链接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        log.debug("用户[{}]已断开连接！", userId);
        webSockets.remove(this);
        sessionPool.remove(this.userId);
        onClose(userId);
        onlineCount();
    }

    @Override
    public long onlineCount() {
        log.debug("连接总数为: {}", webSockets.size());
        return webSockets.size();
    }

    /**
     * 接收文字数据
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(String message) {
        onMessage(userId, message);
    }

    /**
     * 收到客户端消息后调用的方法(二进制)
     */
    @OnMessage
    public void onMessage(byte[] message) {
        onMessage(userId, message);
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public void sendMessage(WebSocketMessagePayload message) {
        if (!ObjectUtils.isEmpty(message)) {
            log.debug("【websocket消息】 消息:{}", JSONObject.toJSONString(message));
            LinkedHashSet<Long> messageToUserId = new LinkedHashSet<>(message.getToUser().stream().map(MessageUser::userId).toList());
            if (CollectionUtil.isEmpty(messageToUserId)) {
                sendAllMessage(message);
            } else {
                for (Long userId : messageToUserId) {
                    Session session = sessionPool.get(userId);
                    if (session != null && session.isOpen()) {
                        try {
                            send(message, session);
                        } catch (Exception e) {
                            log.error("消息发送失败！", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 发送广播消息
     *
     * @param message 消息
     */
    @Override
    public void sendAllMessage(WebSocketMessagePayload message) {
        for (AbstractWebSocketServer webSocket : webSockets) {
            try {
                if (webSocket.session.isOpen()) {
                    send(message, webSocket.session);
                }
            } catch (Exception e) {
                log.error("广播消息失败！", e);
            }
        }
    }

    /**
     * 发送
     *
     * @param message 消息
     * @param session session
     */
    public void send(WebSocketMessagePayload message, Session session) {
        if (message.getByteBuffer() != null) {
            session.getAsyncRemote().sendBinary(message.getByteBuffer());
        } else {
            session.getAsyncRemote().sendText(JSONObject.toJSONString(message));
        }
    }
}
