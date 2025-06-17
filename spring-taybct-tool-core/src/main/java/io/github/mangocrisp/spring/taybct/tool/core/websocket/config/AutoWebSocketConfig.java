package io.github.mangocrisp.spring.taybct.tool.core.websocket.config;

import jakarta.websocket.Session;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * <pre>
 * WebSocket
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/3/14 15:30
 */
@AutoConfiguration
@ConditionalOnClass(Session.class)
public class AutoWebSocketConfig {

    /**
     * 注入ServerEndpointExporter，
     * 这个bean会自动注册使用了@ServerEndpoint注解声明的Websocket endpoint
     */
    @Bean
    @ConditionalOnMissingBean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
