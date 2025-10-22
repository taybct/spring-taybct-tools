package io.github.taybct.tool.core.doc;

import cn.hutool.core.convert.Convert;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
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
@EnableConfigurationProperties({DocProperties.class})
@ConditionalOnClass(OpenAPI.class)
public class DocConfig {

    // 从配置文件读取应用名称和版本（可在application.yml中配置）
    @Value("${spring.application.name:应用接口文档}")
    private String applicationName;

    @Value("${application.version:v3.5.0}")
    private String applicationVersion;

    @Bean
    public OpenAPI restfulOpenAPI(DocProperties docProperties) {
        // 全局添加安全要求（所有接口默认需要JWT认证）
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
        String title = Convert.toStr(docProperties.getTitle(), applicationName);
        String description = Convert.toStr(docProperties.getDescription(), "基于 SpringDoc OpenAPI 自动生成的接口文档，包含所有可用接口的详细信息和测试功能");
        String version = Convert.toStr(docProperties.getVersion(), applicationVersion);
        DocProperties.Contact contact = docProperties.getContact();
        DocProperties.License license = docProperties.getLicense();

        // 定义JWT安全方案
        DocProperties.Authorization authorization = docProperties.getAuthorization();
        SecurityScheme securityScheme = new SecurityScheme()
                .type(authorization.getType())
                .scheme(authorization.getScheme())
                .bearerFormat(authorization.getBearerFormat())
                .in(authorization.getIn())
                .name(authorization.getName())
                .extensions(authorization.getExtensions());

        return new OpenAPI()
                // 文档基本信息
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contact.getName())
                                .url(contact.getUrl())
                                .email(contact.getEmail())
                                .extensions(contact.getExtensions()))
                        .license(new License()
                                .name(license.getName())
                                .url(license.getUrl())
                                .extensions(license.getExtensions()))
                        .extensions(docProperties.getExtensions()))
                // 服务器配置（可配置多环境，如开发、测试、生产）
                .servers(docProperties.getServers().stream().map(server -> new Server()
                                .url(server.getUrl())
                                .description(server.getDescription())
                                .variables(server.getVariables())
                                .extensions(server.getExtensions()))
                        .toList())
                // 添加安全方案
                .components(new Components().addSecuritySchemes(authorization.getSecuritySchemesKey(), securityScheme))
                // 全局应用安全要求
                .addSecurityItem(securityRequirement);
    }

}
