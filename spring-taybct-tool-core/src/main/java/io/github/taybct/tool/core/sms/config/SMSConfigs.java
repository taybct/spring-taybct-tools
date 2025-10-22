package io.github.taybct.tool.core.sms.config;

import cn.hutool.core.util.ObjectUtil;
import io.github.taybct.tool.core.sms.constants.CacheConstants;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 多种短信配置
 *
 * @author xijieyin <br> 2022/8/5 22:46
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ConditionalOnProperty(value = "sms")
@ConfigurationProperties(prefix = "sms")
public class SMSConfigs {

    /**
     * 参数定义
     */
    public interface def {
        /**
         * 默认超时 毫秒 5分钟
         */
        Long DEFAULT_TIME_OUT = 5L * 60 * 1000;
        /**
         * 验证码存放的redis里面的key
         */
        String SMS_VERIFY = CacheConstants.SMS.VERIFY;
        /**
         * 每日最大短信数
         */
        Integer MAX_SEND_TIME = 10;
    }

    /**
     * 访问密钥
     */
    private String accessKeyId;
    /**
     * 加密
     */
    private String accessKeySecret;
    /**
     * 缓存超时 毫秒
     */
    private Long timeOut = def.DEFAULT_TIME_OUT;
    /**
     * 每日最大短信数
     */
    private Integer maxSendTime = def.MAX_SEND_TIME;

    /**
     * 所有的配置
     */
    private Map<String, SMSConfig> configs;

    /**
     * 使用公共的属性
     *
     * @param configs 所有的配置
     * @return Map&lt;SMSConfig&gt;
     * @author xijieyin <br> 2022/8/5 22:47
     * @since 1.0.0
     */
    public Map<String, SMSConfig> useCommonProperties(Map<String, SMSConfig> configs) {
        if (ObjectUtil.isNotEmpty(configs)) {
            configs.forEach((key, config) -> {
                //设置默认的缓存key
                if (ObjectUtil.isEmpty(config.getCachePrefix())) {
                    config.setCachePrefix(def.SMS_VERIFY);
                }
            });
        }
        return configs;
    }

    public void setConfigs(Map<String, SMSConfig> configs) {
        this.configs = useCommonProperties(configs);
    }

    /**
     * 根据 key 获取到不同的模板配置
     *
     * @param key 键
     * @return SMSConfig
     * @author xijieyin <br> 2022/8/5 22:47
     * @since 1.0.0
     */
    public SMSConfig getConfigs(String key) {
        SMSConfig smsConfig = configs.get(key);
        if (ObjectUtil.isNull(smsConfig)) {
            return null;
        }
        if (ObjectUtil.isNull(smsConfig.getAccessKeyId())) {
            smsConfig.setAccessKeyId(this.accessKeyId);
        }
        if (ObjectUtil.isNull(smsConfig.getAccessKeySecret())) {
            smsConfig.setAccessKeySecret(this.accessKeySecret);
        }
        if (ObjectUtil.isNull(smsConfig.getTimeOut())) {
            smsConfig.setTimeOut(this.timeOut);
        }
        return smsConfig;
    }
}
