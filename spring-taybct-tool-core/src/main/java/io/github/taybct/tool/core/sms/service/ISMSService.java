package io.github.taybct.tool.core.sms.service;

import com.alibaba.fastjson2.JSONObject;
import io.github.taybct.tool.core.sms.enums.ConfigType;

/**
 * 短信操作 service
 *
 * @author xijieyin <br> 2022/8/5 22:52
 * @since 1.0.0
 */
public interface ISMSService {

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号码
     * @param configType  类型
     * @author xijieyin <br> 2022/8/5 22:52
     * @since 1.0.0
     */
    boolean smsSendVerify(String phoneNumber, ConfigType configType);

    /**
     * 发送短信
     *
     * @param phoneNumber 电话号码
     * @param configType  操作类型
     * @param message     消息内容 json
     * @return boolean
     * @author xijieyin <br> 2022/8/5 22:52
     * @since 1.0.0
     */
    boolean smsSendMessage(String phoneNumber, ConfigType configType, JSONObject message) throws Exception;

    /**
     * 查询当天发送记录数
     *
     * @param phoneNumber 手机
     * @param configType  发送类型
     * @return JSONObject
     * @author xijieyin <br> 2022/8/5 22:53
     * @since 1.0.0
     */
    JSONObject countToday(String phoneNumber, ConfigType configType);
}
