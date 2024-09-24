package io.github.mangocrisp.spring.taybct.tool.core.util.rsa;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xijieyin <br> 2022/10/12 11:19
 * @since 1.0.5
 */
@Data
@ConfigurationProperties(PropertiesPrefixConstants.RSA)
public class RSAProperties implements Serializable {

    private static final long serialVersionUID = -7565250751606396689L;

    /**
     * 证书路径
     */
    private String resource = "rsa.jks";
    /**
     * 生成证书的时候配置的 alias
     */
    private String alias = "rsa";
    /**
     * 生成证书的时候配置的 密码
     */
    private String password = "taybct";
    /**
     * 过期检查
     */
    private Boolean expireCheck = true;
    /**
     * 解密对类型
     */
    private Map<String, RSAProperties> type = new ConcurrentHashMap<>();

}
