package io.github.mangocrisp.spring.taybct.tool.core.doc;

import io.github.mangocrisp.spring.taybct.tool.core.constant.AuthHeaderConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 文档配置
 *
 * @author xijieyin <br> 2023/2/28 下午11:35
 */
@AutoConfiguration
@EnableConfigurationProperties({SwaggerProperties.class})
@ConditionalOnClass(OpenAPI.class)
public class DocConfig {

    @Bean
    public OpenAPI restfulOpenAPI(SwaggerProperties swaggerProperties) {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(AuthHeaderConstants.AUTHORIZATION_KEY, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info()
                        .title(swaggerProperties.getTitle())
                        .description(swaggerProperties.getDescription())
                        .version(swaggerProperties.getVersion())
                );
    }

}
