package io.github.mangocrisp.spring.taybct.tool.core.mq.handl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 通过实现 ReturnCallback 接口，启动消息失败返回，比如路由不到队列时触发回调
 */
@Slf4j
public class ReturnCallBackHandler implements RabbitTemplate.ReturnsCallback {

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.debug("消息主体：" + returnedMessage.getMessage());
        log.debug("应答码：" + returnedMessage.getReplyCode());
        log.debug("原因描述：" + returnedMessage.getReplyText());
        log.debug("交换机：" + returnedMessage.getExchange());
        log.debug("消息使用的路由键：" + returnedMessage.getRoutingKey());
    }

}
