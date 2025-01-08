package io.github.mangocrisp.spring.taybct.tool.core.sms.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.sms.config.SMSConfig;
import io.github.mangocrisp.spring.taybct.tool.core.sms.config.SMSConfigs;
import io.github.mangocrisp.spring.taybct.tool.core.sms.config.SMSUtil;
import io.github.mangocrisp.spring.taybct.tool.core.sms.enums.ConfigType;
import io.github.mangocrisp.spring.taybct.tool.core.sms.service.ISMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 短信 service 实现
 *
 * @author xijieyin <br> 2022/8/5 22:52
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Service
@AutoConfiguration
@Slf4j
@EnableConfigurationProperties({SMSConfigs.class})
@ConditionalOnClass({RedisTemplate.class})
public class SMSServiceImpl implements ISMSService {

    final SMSConfigs smsConfigs;

    final RedisTemplate<Object, Object> redisTemplate;

    @Override
    public boolean smsSendVerify(String phoneNumber, ConfigType configType) {
        JSONObject message = new JSONObject();
        Integer code = RandomUtil.randomInt(100000, 999999);
        message.put("code", code);
        log.trace(message.toJSONString());

        SMSConfig smsConfig = smsConfigs.getConfigs(configType.getType());

        String prefix = smsConfig.getCachePrefix() + phoneNumber;
        redisTemplate.opsForValue().set(prefix, code.toString(), smsConfig.getTimeOut(), TimeUnit.MILLISECONDS);
        try {
            JSONObject body = countToday(phoneNumber, configType);
            float totalCount = body.getFloat("totalCount");
            if (totalCount >= smsConfigs.getMaxSendTime()) {
                throw new RuntimeException((String.format("该手机已达今日可发送短信次数上限（%s）！", smsConfigs.getMaxSendTime())));
            }
            // TODO 测试的时候把这行注释掉，就可以了，不用真去发短信
            smsSendMessage(phoneNumber, configType, message);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean smsSendMessage(String phoneNumber, ConfigType configType, JSONObject message) throws Exception {
        SMSConfig smsConfig = smsConfigs.getConfigs(configType.getType());
        JSONObject response = SMSUtil.ini(smsConfig).sendSms(phoneNumber, message.toJSONString());
        if (response.getString("code").equalsIgnoreCase("OK")) {
            return true;
        }
        throw new RuntimeException(String.format("短信发送失败：%s", response.toJSONString()));
    }

    @Override
    public JSONObject countToday(String phoneNumber, ConfigType configType) {
        SMSConfig smsConfig = smsConfigs.getConfigs(configType.getType());
        try {
            Object body = SMSUtil.ini(smsConfig).querySendDetails(phoneNumber, DateUtil.format(new Date(), "yyyyMMdd"));
            return JSONObject.parseObject(JSONObject.toJSONString(body));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
