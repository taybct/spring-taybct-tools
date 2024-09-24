package io.github.mangocrisp.spring.taybct.tool.core.message;

import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertiesPrefixConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.unit.DataSize;

import java.io.Serializable;


/**
 * Api 日志配置
 *
 * @author xijieyin <br> 2023/2/23 下午5:36
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = PropertiesPrefixConstants.MESSAGE)
public class MessageProperties implements Serializable {

    private static final long serialVersionUID = -3057115416752574923L;

    /**
     * 是否开启日志
     */
    private Boolean enable = true;
    /**
     * 临时日志文件夹
     */
    private String folder = "temp/message";
    /**
     * 最多存储多少天的数据，默认 15 天
     */
    private Long maxHistory = 15L;
    /**
     * 本地日志文件检查频率
     */
    private Long checkDelay = 1000L * 10;
    /**
     * 缓存，即池子满了这个数量就会直接发送消息，而不用等待
     */
    private Long buffer = 1000L;
    /**
     * 日志发送频率
     */
    private Long delay = 1000L * 60;

}
