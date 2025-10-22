package io.github.taybct.tool.core.sms.config;

import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 短信配置信息
 *
 * @author xijieyin <br> 2022/8/5 22:46
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class SMSConfig {
    /**
     * 访问密钥
     */
    private String accessKeyId;
    /**
     * 加密
     */
    private String accessKeySecret;
    /**
     * 缓存key前缀
     */
    private String cachePrefix;
    /**
     * 缓存超时 毫秒
     */
    private Long timeOut;
    /**
     * 页面大小
     */
    private Long pageSize = 20L;
    /**
     * 当前页
     */
    private Long currentPage = 1L;

    /**
     * 发送消息请求体
     */
    private SendSmsRequest sendSmsRequest;
    /**
     * 查询记录请求体
     */
    private QuerySendDetailsRequest querySendDetailsRequest;
}
