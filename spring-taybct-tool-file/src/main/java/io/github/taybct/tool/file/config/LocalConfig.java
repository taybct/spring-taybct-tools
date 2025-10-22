package io.github.taybct.tool.file.config;

import io.github.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 本地对象存储
 *
 * @author xijieyin <br> 2022/8/4 16:13
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@RefreshScope
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE + ".local")
public class LocalConfig {
    /**
     * 资源映射路径 前缀
     */
    public String localFilePrefix;

    /**
     * 域名或本机访问地址
     */
    public String url;

    /**
     * 上传文件存储在本地的根路径
     */
    private String localFilePath;

    /**
     * 上传文件大小 MB ，小于 0 表示不限制
     */
    private Long uploadFileSize = 50L * 1024 * 1024;
    /**
     * 上传文件名长度限制
     */
    private Integer uploadFileNameLength = 100;
    /**
     * 允许上传的文件类型，不配置表示允许所有
     */
    private String[] uploadAllowedExtensions = null;

    public void setUploadFileSize(Long uploadFileSize) {
        this.uploadFileSize = (uploadFileSize * 1024 * 1024);
    }

    public void setUploadAllowedExtensions(String[] uploadAllowedExtensions) {
        this.uploadAllowedExtensions = uploadAllowedExtensions;
    }
}
