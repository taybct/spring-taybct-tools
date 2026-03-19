package io.github.taybct.tool.file.config;

import cn.hutool.core.codec.Base64;
import io.github.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * <pre>
 * RustFS 配置信息
 * </pre>
 *
 * @author xijieyin
 * @since 2026/1/30 15:52
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@RefreshScope
@ToString
@ConfigurationProperties(prefix = PropertiesPrefixConstants.FILE + ".rustfs")
public class RustFSConfig {

    /**
     * 服务地址
     */
    private String url;

    /**
     * 访问密钥
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
     * @param config 配置信息
     * @return 返回拼接好的 key
     */
    public static String genKey(RustFSConfig config) {
        return Base64.encodeUrlSafe((String.format("%s:%s:%s", config.getUrl(), config.getAccessKey(), config.getSecretKey()).getBytes()));
    }

    /**
     * S3 客户端缓存池
     */
    public static final ConcurrentHashMap<String, S3Client> s3ClientPool = new ConcurrentHashMap<>();

}
