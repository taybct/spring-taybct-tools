package io.github.mangocrisp.spring.taybct.tool.core.sms.config;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信工具类
 *
 * @author xijieyin <br> 2022/8/5 22:48
 * @since 1.0.0
 */
@Slf4j
public class SMSUtil {

    private static final Map<String, Client> clientPool = new ConcurrentHashMap<>();

    /**
     * {@literal 使用AK&SK初始化账号Client}
     *
     * @param accessKeyId     阿里提供的 key
     * @param accessKeySecret 阿里提供的密钥
     * @return Client
     * @author xijieyin <br> 2022/8/5 22:48
     * @since 1.0.0
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Client client = clientPool.get(accessKeyId);
        if (client == null) {
            Config config = new Config()
                    // 您的AccessKey ID
                    .setAccessKeyId(accessKeyId)
                    // 您的AccessKey Secret
                    .setAccessKeySecret(accessKeySecret);
            // 访问的域名
            config.endpoint = "dysmsapi.aliyuncs.com";
            client = new Client(config);
            clientPool.put(accessKeyId, client);
        }
        return client;
    }

    public static SendSmsResponse sendSms(
            String accessKeyId,
            String accessKeySecret,
            String phoneNumbers,
            String signName,
            String templateCode) throws Exception {
        Client client = createClient(accessKeyId, accessKeySecret);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phoneNumbers)
                .setSignName(signName)
                .setTemplateCode(templateCode).setTemplateParam("{\"title\":\"123456\"}");
        // 复制代码运行请自行打印 API 的返回值
        return client.sendSms(sendSmsRequest);
    }

    private Client client;
    private SendSmsRequest sendSmsRequest;
    private QuerySendDetailsRequest querySendDetailsRequest;

    public void setClient(Client client) {
        this.client = client;
    }

    public void setSendSmsRequest(SendSmsRequest sendSmsRequest) {
        this.sendSmsRequest = sendSmsRequest;
    }

    public void setQuerySendDetailsRequest(QuerySendDetailsRequest querySendDetailsRequest) {
        this.querySendDetailsRequest = querySendDetailsRequest;
    }

    /**
     * 初始化客户端
     *
     * @param config 配置信息
     * @return {@link SMSUtil}
     * @author xijieyin <br> 2022/8/5 22:48
     * @since 1.0.0
     */
    public static SMSUtil ini(SMSConfig config) throws Exception {
        SMSUtil smsUtils = new SMSUtil();
        smsUtils.setClient(createClient(config.getAccessKeyId(), config.getAccessKeySecret()));
        smsUtils.setSendSmsRequest(config.getSendSmsRequest());
        smsUtils.setQuerySendDetailsRequest(ObjectUtil.isEmpty(config.getQuerySendDetailsRequest())
                ? new QuerySendDetailsRequest().setPageSize(config.getPageSize()).setCurrentPage(config.getCurrentPage())
                : config.getQuerySendDetailsRequest());
        return smsUtils;
    }

    /**
     * 发送信息
     *
     * @param phoneNumbers 手机号码
     * @param message      信息内容，根据模板来的json
     * @return JSONObject
     * @author xijieyin <br> 2022/8/5 22:49
     * @see com.aliyun.dysmsapi20170525.models.SendSmsResponse
     * @since 1.0.0
     */
    public JSONObject sendSms(String phoneNumbers, String message) throws Exception {
        sendSmsRequest.setPhoneNumbers(phoneNumbers).setTemplateParam(message);
        SendSmsResponse response = client.sendSms(sendSmsRequest);
        //		log.info(result.toJSONString());
        return JSONObject.parseObject(JSONObject.toJSONString(response.getBody()));
    }

    /**
     * 查询记录
     *
     * @param phoneNumber 手机号码
     * @param sendDate    日期
     * @return Object
     * @author xijieyin <br> 2022/8/5 22:50
     * @see com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse
     * @since 1.0.0
     */
    public Object querySendDetails(String phoneNumber, String sendDate) throws Exception {
        querySendDetailsRequest.setPhoneNumber(phoneNumber).setSendDate(sendDate);
        return client.querySendDetails(querySendDetailsRequest).getBody();
    }

}
