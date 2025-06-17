package io.github.mangocrisp.spring.taybct.tool.core.websocket.endpoint;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.ServerReactiveEndpoint;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.enums.MessageUserType;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.MessageUser;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebSocketMessagePayload;
import io.github.mangocrisp.spring.taybct.tool.core.websocket.support.WebsocketReactiveSession;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    protected static final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebsocketReactiveSession>> userWebSocketRecordPool = new ConcurrentHashMap<>();
    /**
     * 存放 websocket 会话
     */
    protected static final ConcurrentHashMap<String, WebsocketReactiveSession> webSocketRecordPool = new ConcurrentHashMap<>();
    /**
     * 存放请求参数
     */
    protected static final ConcurrentHashMap<String, Map<String, List<String>>> requestParameterMap = new ConcurrentHashMap<>();
    /**
     * 存放路径请求参数
     */
    protected static final ConcurrentHashMap<String, Map<String, String>> pathParameterMap = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        Map<String, String> pathParameter = checkPathParameter(handshakeInfo.getUri().getPath());
        Map<String, List<String>> requestParameter = checkRequestParameter(handshakeInfo.getUri().getQuery());
        String userIdStr = getUserId(pathParameter);
        if (!StringUtils.hasText(userIdStr)) {
            throw new RuntimeException("用户id不能为空");
        }
        Long userId = Convert.toLong(userIdStr);
        pathParameterMap.put(session.getId(), pathParameter);
        requestParameterMap.put(session.getId(), requestParameter);

        Mono<Void> input = session.receive()
                .doOnNext(webSocketMessage -> onMessage(getSession(session.getId()), userId, webSocketMessage))
                .doOnError(throwable -> onError(getSession(session.getId()), userId, throwable))
                // 当接收消息流完成时，表示连接已关闭
                .doOnComplete(() -> log.debug("WebSocket 连接已正常关闭: {}", userId))
                .then(session.closeStatus())
                .doOnSuccess(closeStatus -> onStatus(getSession(session.getId()), closeStatus, userId))
                .then()
                .doFinally(signalType -> onFinally(getSession(session.getId()), signalType, userId))
                .then();

        Mono<Void> output = session.send(Flux.create(sink
                -> {
            WebsocketReactiveSession websocketReactiveSession = new WebsocketReactiveSession(session, sink);
            cacheSession(userId, websocketReactiveSession);
            onOpen(userId, websocketReactiveSession);
        }));
        /*
         * Mono.zip() 会将多个 Mono 合并为一个新的 Mono，任何一个 Mono 产生 error 或 complete 都会导致合并后的 Mono
         * 也随之产生 error 或 complete，此时其它的 Mono 则会被执行取消操作。
         */
        return Mono.zip(input, output).then();
    }

    /**
     * 获取用户 id
     *
     * @param pathParameterMap 路径参数
     * @return 用户 id
     */
    public String getUserId(Map<String, String> pathParameterMap) {
        return pathParameterMap.get("userId");
    }

    /**
     * 用于获取路径参数
     *
     * <pre>
     * {@code /websocket/{userId}?toUserId=xxx}
     * </pre>
     *
     * @param path 请求地址
     * @return 路径参数
     */
    public Map<String, String> checkPathParameter(String path) {
        Map<String, String> pathParameterMap = new ConcurrentHashMap<>();
        ServerReactiveEndpoint annotation = getClass().getAnnotation(ServerReactiveEndpoint.class);
        if (annotation!=null){
            String matchPath = annotation.value();
            // 提取变量名
            Pattern varNamePattern = Pattern.compile("\\{([^/]+)}");
            Matcher varNameMatcher = varNamePattern.matcher(matchPath);
            // 构建变量名列表
            List<String> varNames = new ArrayList<>();
            while (varNameMatcher.find()) {
                varNames.add(varNameMatcher.group(1));
            }
            // 构建用于匹配的正则表达式
            String regex = matchPath.replaceAll("\\{[^/]+}", "([^/]+)");
            Pattern pathPattern = Pattern.compile(regex);
            Matcher pathMatcher = pathPattern.matcher(path);
            // 如果匹配成功，将变量名和对应值存入Map
            if (pathMatcher.matches()) {
                for (int i = 0; i < varNames.size(); i++) {
                    pathParameterMap.put(varNames.get(i), pathMatcher.group(i + 1));
                }
                return pathParameterMap;
            }
        }
        return pathParameterMap;
    }

    /**
     * 用于获取请求参数
     *
     * <pre>
     * {@code /websocket/{userId}?toUserId=xxx}
     * </pre>
     *
     * @param query 请求参数
     */
    public Map<String, List<String>> checkRequestParameter(String query) {
        Map<String, List<String>> requestParameterMap = new ConcurrentHashMap<>();
        String[] queryParamArr = query.split("&");
        Arrays.stream(queryParamArr).forEach(queryParam -> {
            String[] kv = queryParam.split("=");
            List<String> values = requestParameterMap.getOrDefault(kv[0], new ArrayList<>());
            values.add(kv[1]);
            requestParameterMap.put(kv[0], values);
        });
        return requestParameterMap;
    }

    @Override
    public void cacheSession(Long userId, WebsocketReactiveSession websocketReactiveSession) {
        CopyOnWriteArraySet<WebsocketReactiveSession> userWebSocketRecordPoolOrDefault = userWebSocketRecordPool.getOrDefault(userId, new CopyOnWriteArraySet<>());
        userWebSocketRecordPoolOrDefault.add(websocketReactiveSession);
        userWebSocketRecordPool.put(userId, userWebSocketRecordPoolOrDefault);
        webSocketRecordPool.put(websocketReactiveSession.session().getId(), websocketReactiveSession);
    }

    @Override
    public CopyOnWriteArraySet<WebsocketReactiveSession> getSession(Long userId) {
        return userWebSocketRecordPool.get(userId);
    }

    @Override
    public WebsocketReactiveSession getSession(String sessionId) {
        return webSocketRecordPool.get(sessionId);
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
     * 连接
     *
     * @param userId 用户 id
     */
    @Override
    public void onOpen(Long userId, WebsocketReactiveSession session) {
        log.debug("用户已连接：{}:{}", userId, session.session().getId());
        onlineCount();
        // 返回告诉客户端连接成功，返回的第一条消息就是 sessionId
        sendSimpleMessage(session.session().getId(), new MessageUser(MessageUserType.USER, userId, session.session().getId()));
    }

    @Override
    public void onError(WebsocketReactiveSession session, Long userId, Throwable error) {
        log.error("WebSocket 异常！{}", userId, error);
    }

    /**
     * 当连接关闭时执行，无论正常关闭还是异常关闭
     *
     * @param session     会话
     * @param closeStatus 根据信号类型处理不同的断开连接情况
     * @param userId      用户 id
     */
    public void onStatus(WebsocketReactiveSession session, CloseStatus closeStatus, Long userId) {
        log.debug("WebSocket 状态码: {}, 原因: {}", closeStatus.getCode(), closeStatus.getReason());
        // 根据关闭状态处理不同的断开连接情况
        switch (closeStatus.getCode()) {
            case 1000: // 正常关闭
                log.debug("用户正常断开连接: {}:{}", userId, session.session().getId());
                break;
            case 1001: // 客户端离开
                log.debug("用户离开: {}:{}", userId, session.session().getId());
                break;
            case 1006: // 连接异常关闭
                log.debug("连接异常关闭: {}:{}", userId, session.session().getId());
                break;
        }
        // 处理其他状态码...
        onStatus(session, userId, closeStatus.getCode(), closeStatus.getReason());
    }

    /**
     * 最后处理
     *
     * @param session    会话
     * @param signalType 信号类型
     * @param userId     用户 id
     */
    public void onFinally(WebsocketReactiveSession session, SignalType signalType, Long userId) {
        log.debug("用户[{}:{}]已断开连接！", userId, session.session().getId());// 当连接关闭时执行，无论正常关闭还是异常关闭
        if (signalType == SignalType.ON_COMPLETE) {
            log.debug("WebSocket 连接正常关闭: {}:{}", userId, session.session().getId());
        } else if (signalType == SignalType.ON_ERROR) {
            log.debug("WebSocket 连接异常关闭: {}:{}", userId, session.session().getId());
        } else if (signalType == SignalType.CANCEL) {
            log.debug("WebSocket 连接被取消: {}:{}", userId, session.session().getId());
        }
        // 删除用户和 session 的关联
        userWebSocketRecordPool.getOrDefault(userId, new CopyOnWriteArraySet<>()).remove(session);
        // 删除 session
        webSocketRecordPool.remove(session.session().getId());
        // 删除session的请求参数
        requestParameterMap.remove(session.session().getId());
        pathParameterMap.remove(session.session().getId());
        onClose(session, userId);
        onlineCount();
    }

    @Override
    public long onlineCount() {
        log.debug("连接总数为: {}", webSocketRecordPool.size());
        return webSocketRecordPool.size();
    }

    public void onMessage(WebsocketReactiveSession session, Long userId, WebSocketMessage webSocketMessage) {
        if (webSocketMessage.getType().equals(WebSocketMessage.Type.TEXT)) {
            onMessage(session, userId, webSocketMessage.getPayloadAsText(Charset.defaultCharset()));
        }
        if (webSocketMessage.getType().equals(WebSocketMessage.Type.BINARY)) {
            onMessage(session, userId, IoUtil.readBytes(webSocketMessage.getPayload().asInputStream(true)));
        }
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
            if (CollectionUtil.isEmpty(message.getToUser())) {
                sendAllMessage(message);
            } else {
                message.getToUser().forEach(messageToUser -> {
                    if (StringUtils.hasText(messageToUser.sessionId())) {
                        // 如果设置了指定的 session id 则发送给指定的 session
                        sendMessage(message, messageToUser, webSocketRecordPool.get(messageToUser.sessionId()));
                    } else {
                        // 如果只是指定发给某个用户，那这个用户所有联接的会话都会收到消息
                        if (userWebSocketRecordPool.containsKey(messageToUser.userId())) {
                            userWebSocketRecordPool.get(messageToUser.userId()).forEach(session -> sendMessage(message, messageToUser, session));
                        } else {
                            log.warn("用户{}的websocket连接不存在或已断开！", messageToUser.userId());
                        }
                    }
                });
            }
        }
    }

    public void sendMessage(WebSocketMessagePayload message, MessageUser messageToUser, WebsocketReactiveSession session) {
        if (session.session().isOpen()) {
            try {
                send(message, session);
            } catch (Exception e) {
                log.error("消息发送失败！", e);
            }
        } else {
            log.warn("用户{}:{}的websocket连接不存在或已断开！", messageToUser.userId(), session.session().getId());
        }
    }

    @Override
    public void sendAllMessage(WebSocketMessagePayload message) {
        userWebSocketRecordPool.values().stream().flatMap(Collection::stream)
                .forEach(session -> {
                    try {
                        if (session.session().isOpen()) {
                            send(message, session);
                        }
                    } catch (Exception e) {
                        log.error("广播消息失败！", e);
                    }
                });
    }

    @Override
    public void send(WebSocketMessagePayload message, WebsocketReactiveSession session) {
        if (message.getByteBuffer() != null) {
            session.sink().next(session.session().binaryMessage(data -> data.wrap(message.getByteBuffer())));
        } else {
            session.sink().next(session.session().textMessage(JSONObject.toJSONString(message)));
        }
    }

}
