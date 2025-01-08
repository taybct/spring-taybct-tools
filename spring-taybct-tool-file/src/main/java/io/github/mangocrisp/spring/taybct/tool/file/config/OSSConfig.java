package io.github.mangocrisp.spring.taybct.tool.file.config;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serial;
import java.io.Serializable;

/**
 * OSS 配置信息
 *
 * @author LiXiaoMing <br> 2021/3/24 15:47
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@RefreshScope
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE + ".oss")
public class OSSConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = -3957844714727833322L;

    /**
     * 代理地址
     */
    private String proxyUrl;
    /**
     * 临时存储路径（仅需要代理时设置）
     */
    private String tempPath;
    /**
     * 服务地址,文件访问前缀
     */
    private String url;
    /**
     * oss 服务端
     */
    private String endpoint;
    /**
     * oss key
     */
    private String accessKeyId;
    /**
     * oss 密钥
     */
    private String accessKeySecret;
    /**
     * 桶
     */
    private String bucketName;
    /**
     * 使用频率
     */
    private Integer frequency;
    /**
     * cron 任务
     */
    private String task;
    /**
     * 多少天内的数据
     */
    private Integer days;

}
