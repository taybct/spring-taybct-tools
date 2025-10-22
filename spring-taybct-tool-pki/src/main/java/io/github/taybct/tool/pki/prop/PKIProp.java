package io.github.taybct.tool.pki.prop;

import io.github.taybct.tool.core.constant.CacheConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * PKI 配置
 *
 * @author XiJieYin <br> 2023/8/3 16:42
 */
@ConfigurationProperties("pki")
@Data
public class PKIProp implements Serializable {

    @Serial
    private static final long serialVersionUID = 4403124059689073558L;
    /**
     * 认证地址
     */
    private String authUrl;
    /**
     * 应用标识
     */
    private String appId;
    /**
     * 调用应用服务器或网关生成原文
     */
    private Integer randomFrom;
    /**
     * 是否检查访问控制状态
     */
    private boolean accessControl;
    /**
     * 是否开启开维码认证
     */
    private boolean QrCodeAuth;
    /**
     * 生成二维码地址
     */
    private String generateQrCodeUrl;
    /**
     * 查询二维码状态地址
     */
    private String queryQrCodeStateUrl;
    /**
     * 缓存前缀
     */
    private String oauthCachePrefix = CacheConstants.SYSTEM_PREFIX + "oauth:";

}
