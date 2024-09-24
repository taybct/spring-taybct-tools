package io.github.mangocrisp.spring.taybct.tool.core.config;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;

/**
 * Controller 注册 配置
 *
 * @author XiJieYin <br> 2023/7/24 14:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.TAYBCT + ".mvc")
public class ControllerRegisterConfigure {

    /**
     * 需要注册的 Controller
     */
    private LinkedHashSet<Class<?>> registerControllerSet = new LinkedHashSet<>();
    /**
     * Controller 允许的方法，同时也是根据这里的配置来配置对应的 Controller 方法
     */
    private LinkedHashSet<Class<? extends Annotation>> allowMethod = new LinkedHashSet<>();

}
