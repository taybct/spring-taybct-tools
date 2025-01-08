package io.github.mangocrisp.spring.taybct.tool.core.ds.encrypt;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.github.mangocrisp.spring.taybct.tool.core.util.sm.SM4Coder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 加密数据源的配置需要加密哪些字段，默认全加密
 */
@Data
@ConfigurationProperties(prefix = PropertiesPrefixConstants.TAYBCT + ".datasource.encrypted-connection")
public class EncryptedDataSourceProperties {
    /**
     * 是否开启加密，默认是不加密的
     */
    private boolean enabled = false;
    /**
     * 配置统一的解码器
     */
    private Class<? extends Function<String, String>> decryptFunction = SM4Coder.De.class;
    /**
     * 解密方法，为每个数据源 指定一个 {@code Function<String, String>} 类来实现解密,怎么解密要看具体是怎么加密的，例如，sm2 加密的就用 {@code SM2EncryptedPassable}
     *
     * @see io.github.mangocrisp.spring.taybct.tool.sm2.config.SM2EncryptedPassable
     */
    private Map<String, Class<? extends Function<String, String>>> decryptFunctions = new HashMap<>();
    /**
     * JDBC driver
     */
    private boolean driverClassName = true;
    /**
     * JDBC url 地址
     */
    private boolean url = true;
    /**
     * JDBC 用户名
     */
    private boolean username = true;
    /**
     * JDBC 密码
     */
    private boolean password = true;
}
