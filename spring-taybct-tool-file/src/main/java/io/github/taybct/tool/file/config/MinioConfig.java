package io.github.taybct.tool.file.config;

import cn.hutool.core.codec.Base64;
import io.github.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Minio 配置信息
 *
 * @author xijieyin <br> 2022/8/4 16:15
 * @since 1.0.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@RefreshScope
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE + ".minio")
public class MinioConfig {
    /**
     * 服务地址
     */
    private String url;

    /**
     * 用户名
     */
    private String accessKey;

    /**
     * 密码
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 生成缓存池 key 确保唯一
     *
     * @param minioConfig MinIO 配置信息
     * @return 返回拼接好的 key
     */
    public static String genKey(MinioConfig minioConfig) {
        return Base64.encodeUrlSafe((String.format("%s:%s:%s", minioConfig.getUrl(), minioConfig.getAccessKey(), minioConfig.getSecretKey()).getBytes()));
    }

    /**
     * MinIO 客户端缓存池
     */
    public static final ConcurrentHashMap<String, MinioClient> minioClientPool = new ConcurrentHashMap<>();
}
