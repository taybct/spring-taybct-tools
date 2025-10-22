package io.github.taybct.tool.core.version;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 注册到 spring 容器
 *
 * @author xijieyin <br> 2022/10/14 15:28
 * @since 1.0.5
 */
@AutoConfiguration
@ConditionalOnClass(RequestCondition.class)
public class ApiConfiguration implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiMappingHandlerMapping();
    }

}
