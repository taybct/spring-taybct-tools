package io.github.mangocrisp.spring.taybct.tool.core.websocket.support;

import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

import java.io.Serializable;

/**
 * <pre>
 * 保存 WebSocket 连接的 session 以及对应的 FluxSink，以便在 WebSocketHandler 代码范围外发送数据
 * </pre>
 *
 * @param session session
 * @param sink    sink 异步处理
 * @author XiJieYin
 * @since 2025/6/11 14:33
 */
public record WebsocketReactiveSession(WebSocketSession session, FluxSink<WebSocketMessage> sink) implements Serializable {
}
