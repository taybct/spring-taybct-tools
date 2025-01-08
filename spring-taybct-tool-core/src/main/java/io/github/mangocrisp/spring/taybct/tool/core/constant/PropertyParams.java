package io.github.mangocrisp.spring.taybct.tool.core.constant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 从 spring property 里面获取参数
 *
 * @author xijieyin <br> 2022/12/7 9:28
 * @since 2.0.2
 */
@Data
@ConfigurationProperties(prefix = PropertiesPrefixConstants.PARAMS)
@RefreshScope
public class PropertyParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 563981330083199388L;

    private Map<String, Object> map;

}
