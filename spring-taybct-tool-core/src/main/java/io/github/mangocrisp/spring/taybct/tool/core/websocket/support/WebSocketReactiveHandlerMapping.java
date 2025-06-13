package io.github.mangocrisp.spring.taybct.tool.core.websocket.support;

import io.github.mangocrisp.spring.taybct.tool.core.annotation.ServerReactiveEndpoint;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <pre>
 * WebSocketReactiveMapping 注解配置映射
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/6/11 14:25
 */
public class WebSocketReactiveHandlerMapping extends SimpleUrlHandlerMapping {

    private final Map<String, WebSocketHandler> handlerMap = new LinkedHashMap<>();

    @Override
    public void initApplicationContext() throws BeansException {
        Map<String, Object> beanMap = obtainApplicationContext()
                .getBeansWithAnnotation(ServerReactiveEndpoint.class);
        beanMap.values().forEach(bean -> {
            if (!(bean instanceof WebSocketHandler)) {
                throw new RuntimeException(
                        String.format("Controller [%s] doesn't implement WebSocketHandler interface.",
                                bean.getClass().getName()));
            }
            ServerReactiveEndpoint annotation = AnnotationUtils.getAnnotation(bean.getClass(), ServerReactiveEndpoint.class);
            //webSocketMapping 映射到管理中
            handlerMap.put(Objects.requireNonNull(annotation).value(), (WebSocketHandler) bean);
        });
        super.setOrder(Ordered.HIGHEST_PRECEDENCE);
        super.setUrlMap(handlerMap);
        super.initApplicationContext();
    }
}
