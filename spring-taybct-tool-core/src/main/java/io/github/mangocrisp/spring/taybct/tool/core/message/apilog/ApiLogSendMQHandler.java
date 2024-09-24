package io.github.mangocrisp.spring.taybct.tool.core.message.apilog;

import io.github.mangocrisp.spring.taybct.tool.core.message.DefaultMessageType;
import io.github.mangocrisp.spring.taybct.tool.core.message.IMessageSendHandler;
import io.github.mangocrisp.spring.taybct.tool.core.message.MessageProperties;
import io.github.mangocrisp.spring.taybct.tool.core.message.MessageType;
import io.github.mangocrisp.spring.taybct.tool.core.mq.BindingEQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * 接口日志用 mq 发送配置
 *
 * @author xijieyin <br> 2022/8/5 20:21
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ApiLogSendMQHandler implements IMessageSendHandler {

    final MessageProperties apiLogProperties;

    final RabbitTemplate rabbitTemplate;

    @Override
    public MessageType getMessageType() {
        return DefaultMessageType.API_LOG;
    }

    @Override
    public boolean send(String message) {
        log.debug("\r\n==== 接口日志发送延迟队列 \r\n==== {}", message);
        rabbitTemplate.convertAndSend(ApiLogSendMQConfig.def.EXCHANGE,
                BindingEQ.getRoutingKey(ApiLogSendMQConfig.def.KEY), message, m -> {
                    m.getMessageProperties().setHeader("x-delay", apiLogProperties.getDelay());
                    return m;
                });
        return true;
    }
}
