package io.github.mangocrisp.spring.taybct.tool.core.websocket.endpoint;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.MessageUser;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebSocketMessagePayload;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebsocketReactiveSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * 在 WebSocketHandler 代码范围外发送数据
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/6/11 14:33
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractWebSocketReactiveServer implements WebSocketHandler, IWebSocketServer<WebsocketReactiveSession> {
    /**
     * 存放 session
     */
    protected final ConcurrentHashMap<Long, WebsocketReactiveSession> webSocketRecordPool = new ConcurrentHashMap<>();
    /**
     * 存放请求参数
     */
    protected final ConcurrentHashMap<Long, ConcurrentHashMap<String, List<String>>> requestParameterMap = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        ConcurrentHashMap<String, List<String>> requestParameterMap = checkRequestParameter(handshakeInfo.getUri().getPath());
        String userIdStr = requestParameterMap.get("userId").get(0);
        if (!StringUtils.hasText(userIdStr)) {
            throw new RuntimeException("用户id不能为空");
        }
        Long userId = Convert.toLong(userIdStr);
        this.requestParameterMap.put(userId, requestParameterMap);
        Mono<Void> input = session.receive()
                .doOnNext(webSocketMessage -> onMessage(userId, webSocketMessage))
                .doOnError(throwable -> onError(userId, throwable))
                // 当接收消息流完成时，表示连接已关闭
                .doOnComplete(() -> log.debug("WebSocket 连接已正常关闭: {}", userId))
                .then(session.closeStatus())
                .doOnSuccess(closeStatus -> onStatus(closeStatus, userId))
                .then()
                .doFinally(signalType -> onFinally(signalType, userId))
                .then();

        Mono<Void> output = session.send(Flux.create(sink
                -> addWebSocketRecord(userId, new WebsocketReactiveSession(session, sink))));
        /*
         * Mono.zip() 会将多个 Mono 合并为一个新的 Mono，任何一个 Mono 产生 error 或 complete 都会导致合并后的 Mono
         * 也随之产生 error 或 complete，此时其它的 Mono 则会被执行取消操作。
         */
        return Mono.zip(input, output).then();
    }

    /**
     * 用于获取url参数，这里默认使用 path 参数获取用户 id，例如：
     * <pre>
     * {@code /websocket/{userId}}
     * </pre>
     * 获取最后一位的参数作为用户 id，你可以重写这个方法用来获取参数，比如 query 参数
     *
     * @param path url
     */
    public ConcurrentHashMap<String, List<String>> checkRequestParameter(String path) {
        ConcurrentHashMap<String, List<String>> requestParameterMap = new ConcurrentHashMap<>();
        Map<String, String> queryMap = new HashMap<>();
        if (StringUtils.hasText(path)) {
            String[] queryParam = path.split("/");
            requestParameterMap.put("userId", Collections.singletonList(queryParam[queryParam.length - 1]));
        }
        return requestParameterMap;
    }

    /**
     * 获取请求参数
     *
     * @param userId 用户 id
     * @return 请求参数
     */
    public ConcurrentHashMap<String, List<String>> getRequestParameterMap(Long userId) {
        return this.requestParameterMap.get(userId);
    }

    /**
     * 添加 session
     *
     * @param userId                   用户 id
     * @param websocketReactiveSession session
     */
    public void addWebSocketRecord(Long userId, WebsocketReactiveSession websocketReactiveSession) {
        this.webSocketRecordPool.put(userId, websocketReactiveSession);
        onOpen(userId);
    }

    @Override
    public WebsocketReactiveSession getSession(Long userId) {
        return this.webSocketRecordPool.get(userId);
    }

    /**
     * 连接
     *
     * @param userId 用户 id
     */
    @Override
    public void onOpen(Long userId) {
        log.debug("用户已连接：{}", userId);
        onlineCount();
    }

    @Override
    public void onError(Long userId, Throwable error) {
        log.error("WebSocket 异常！{}", userId, error);
    }

    /**
     * 当连接关闭时执行，无论正常关闭还是异常关闭
     *
     * @param closeStatus 根据信号类型处理不同的断开连接情况
     * @param userId      用户 id
     */
    public void onStatus(CloseStatus closeStatus, Long userId) {
        log.debug("WebSocket 状态码: {}, 原因: {}", closeStatus.getCode(), closeStatus.getReason());
        // 根据关闭状态处理不同的断开连接情况
        switch (closeStatus.getCode()) {
            case 1000: // 正常关闭
                log.debug("用户正常断开连接: {}", userId);
                break;
            case 1001: // 客户端离开
                log.debug("用户离开: {}", userId);
                break;
            case 1006: // 连接异常关闭
                log.debug("连接异常关闭: {}", userId);
                break;
        }
        // 处理其他状态码...
        onStatus(userId, closeStatus.getCode(), closeStatus.getReason());
    }

    /**
     * 最后处理
     *
     * @param signalType 信号类型
     * @param userId     用户 id
     */
    public void onFinally(SignalType signalType, Long userId) {
        log.debug("用户[{}]已断开连接！", userId);// 当连接关闭时执行，无论正常关闭还是异常关闭
        if (signalType == SignalType.ON_COMPLETE) {
            log.debug("WebSocket 连接正常关闭: {}", userId);
        } else if (signalType == SignalType.ON_ERROR) {
            log.debug("WebSocket 连接异常关闭: {}", userId);
        } else if (signalType == SignalType.CANCEL) {
            log.debug("WebSocket 连接被取消: {}", userId);
        }
        this.webSocketRecordPool.remove(userId);
        onClose(userId);
        onlineCount();
    }

    @Override
    public long onlineCount() {
        log.debug("连接总数为: {}", this.webSocketRecordPool.size());
        return this.webSocketRecordPool.size();
    }

    public void onMessage(Long userId, WebSocketMessage webSocketMessage) {
        if (webSocketMessage.getType().equals(WebSocketMessage.Type.TEXT)) {
            onMessage(userId, webSocketMessage.getPayloadAsText(Charset.defaultCharset()));
        }
        if (webSocketMessage.getType().equals(WebSocketMessage.Type.BINARY)) {
            onMessage(userId, IoUtil.readBytes(webSocketMessage.getPayload().asInputStream(true)));
        }
    }

    @Override
    public void sendMessage(WebSocketMessagePayload message) {
        if (!ObjectUtils.isEmpty(message)) {
            log.debug("【websocket消息】 消息:{}", JSONObject.toJSONString(message));
            LinkedHashSet<Long> messageToUserId = new LinkedHashSet<>(message.getToUser().stream().map(MessageUser::userId).toList());
            if (CollectionUtil.isEmpty(messageToUserId)) {
                sendAllMessage(message);
            } else {
                for (Long userId : messageToUserId) {
                    WebsocketReactiveSession websocketReactiveSession = this.webSocketRecordPool.get(userId);
                    if (websocketReactiveSession != null && websocketReactiveSession.session().isOpen()) {
                        try {
                            send(message, websocketReactiveSession);
                        } catch (Exception e) {
                            log.error("消息发送失败！", e);
                        }
                    } else {
                        log.warn("用户{}的websocket连接不存在或已断开！", userId);
                    }
                }
            }
        }
    }

    @Override
    public void sendAllMessage(WebSocketMessagePayload message) {
        for (WebsocketReactiveSession websocketReactiveSession : this.webSocketRecordPool.values()) {
            try {
                if (websocketReactiveSession.session().isOpen()) {
                    send(message, websocketReactiveSession);
                }
            } catch (Exception e) {
                log.error("广播消息失败！", e);
            }
        }
    }

    @Override
    public void send(WebSocketMessagePayload message, WebsocketReactiveSession websocketReactiveSession) {
        if (message.getByteBuffer() != null) {
            websocketReactiveSession.sink().next(websocketReactiveSession.session().binaryMessage(data -> data.wrap(message.getByteBuffer())));
        } else {
            websocketReactiveSession.sink().next(websocketReactiveSession.session().textMessage(JSONObject.toJSONString(message)));
        }
    }

}
