package io.github.mangocrisp.spring.taybct.tool.file.config;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.github.mangocrisp.spring.taybct.tool.file.service.IFileService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

/**
 * 对象存储配置
 *
 * @author xijieyin <br> 2022/8/4 16:12
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RefreshScope
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE)
public class FileConfig {

    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 文件服务器类型
     */
    private String type;
    /**
     * 文件服务处理实现类型
     */
    private Class<? extends IFileService> clazz;
    /**
     * /**
     * 文件类型配置
     */
    private Map<String, String> fileContentType = new HashMap<>();

}
