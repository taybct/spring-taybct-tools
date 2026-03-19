package io.github.taybct.tool.core.message.scheduledLogCentralized;

import io.github.taybct.tool.core.message.DefaultMessageType;
import io.github.taybct.tool.core.message.IMessageSendHandler;
import io.github.taybct.tool.core.message.MessageProperties;
import io.github.taybct.tool.core.message.MessageType;
import io.github.taybct.tool.core.message.apilog.ApiLogSendMQConfig;
import io.github.taybct.tool.core.mq.BindingEQ;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.lang.NonNull;

/**
 * 定时任务管理日志用 mq 发送配置
 *
 * @author xijieyin <br> 2022/8/5 20:21
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ScheduledLogCentralizedSendMQHandler implements IMessageSendHandler {

    final MessageProperties apiLogProperties;

    final RabbitTemplate rabbitTemplate;

    @NonNull
    @Override
    public MessageType getMessageType() {
        return DefaultMessageType.SCHEDULED_LOG_CENTRALIZED;
    }

    @Override
    public boolean send(String message) {
        log.debug("\r\n==== 定时任务管理日志 发送延迟队列 \r\n==== {}", message);
        rabbitTemplate.convertAndSend(ScheduledLogCentralizedSendMQConfig.def.EXCHANGE,
                BindingEQ.getRoutingKey(ScheduledLogCentralizedSendMQConfig.def.KEY), message, m -> {
                    m.getMessageProperties().setHeader("x-delay", apiLogProperties.getDelay());
                    return m;
                });
        return true;
    }
}
