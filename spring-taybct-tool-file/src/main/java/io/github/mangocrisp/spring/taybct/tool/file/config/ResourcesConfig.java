package io.github.mangocrisp.spring.taybct.tool.file.config;

import javax.annotation.Resource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 通用映射配置，主要用于，本地存储文件来管理文件时，需要把文件映射出去请求，出于安全考虑，虽然提供了这样的功能，但是不建议使用这样的方式
 *
 * @author xijieyin <br> 2022/8/4 16:17
 * @since 1.0.0
 */
@AutoConfiguration
public class ResourcesConfig implements WebMvcConfigurer {

    @Resource
    LocalConfig localConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /* 本地文件上传路径 */
        registry.addResourceHandler(localConfig.getLocalFilePrefix() + "/**")
                .addResourceLocations("file:" + localConfig.getLocalFilePath() + File.separator);
    }

}
