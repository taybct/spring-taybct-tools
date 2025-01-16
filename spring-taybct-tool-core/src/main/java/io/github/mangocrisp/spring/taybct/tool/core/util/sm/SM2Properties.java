package io.github.mangocrisp.spring.taybct.tool.core.util.sm;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sm2 加密配置
 *
 * @author xijieyin <br> 2022/10/12 11:19
 * @since 2.4.0-beta.11
 */
@Data
@ConfigurationProperties(PropertiesPrefixConstants.SM2)
public class SM2Properties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7196627241730510697L;

    /**
     * 证书路径
     */
    private String resource = "sm2.jks";
    /**
     * 生成证书的时候配置的 alias
     */
    private String alias = "sm2";
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
    private Map<String, SM2Properties> type = new ConcurrentHashMap<>();

}
