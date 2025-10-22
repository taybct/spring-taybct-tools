package io.github.taybct.tool.core.message.apilog;

import io.github.taybct.tool.core.message.IMessageSendService;
import io.github.taybct.tool.core.message.MessageProperties;
import io.github.taybct.tool.core.mq.BindingEQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * rabbit mq 自动创建队列配置
 *
 * @author xijieyin <br> 2022/8/5 20:17
 * @since 1.0.0
 */
@AutoConfiguration
@EnableRabbit
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class ApiLogSendMQConfig {

    /**
     * exchange 和 queue 的 key
     */
    public interface def {
        /**
         * 接口日志
         */
        String KEY = "API_LOG";
        String EXCHANGE = BindingEQ.def.prefix + "." + KEY + "." + BindingEQ.def.exchange;
        String QUEUE = BindingEQ.def.prefix + "." + KEY + "." + BindingEQ.def.queue;

    }

    /**
     * <pre>
     * 配置队列
     * </pre>
     *
     * @return Queue
     * @author xijieyin
     * @since 2024/9/1 23:46
     */
    @Bean("apiLogQueue")
    public Queue apiLogQueue() {
        //属性参数 队列名称 是否持久化
        return new Queue(def.QUEUE, true);
    }

    /**
     * <pre>
     * 配置队列交换机
     * </pre>
     *
     * @return CustomExchange
     * @author xijieyin
     * @since 2024/9/1 23:47
     */
    @Bean("apiLogCustomExchange")
    public CustomExchange apiLogCustomExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        //属性参数 交换机名称 交换机类型 是否持久化 是否自动删除 配置参数
        return new CustomExchange(def.EXCHANGE, "x-delayed-message", true, false, args);
    }

    /**
     * <pre>
     * 队列绑定交换机
     * </pre>
     *
     * @param apiLogCustomExchange 交换机
     * @param apiLogQueue          队列
     * @return Binding
     * @author xijieyin
     * @since 2024/9/1 23:47
     */
    @Bean("apiLogBinding")
    public Binding apiLogBinding(@Qualifier("apiLogCustomExchange") CustomExchange apiLogCustomExchange
            , @Qualifier("apiLogQueue") Queue apiLogQueue) {
        return BindingBuilder.bind(apiLogQueue)
                .to(apiLogCustomExchange)
                .with(BindingEQ.getRoutingKey(def.KEY)).noargs();
    }

    /**
     * <pre>
     * 配置消息发送处理器
     * </pre>
     *
     * @param messageProperties 配置
     * @param rabbitTemplate    rabbit mq
     * @return SysFileLinkSendMQHandler
     * @author xijieyin
     * @since 2024/9/1 23:57
     */
    @Bean("apiLogMessageSendHandler")
    @ConditionalOnMissingBean(ApiLogSendMQHandler.class)
    public ApiLogSendMQHandler scheduledLogSendMQHandler(MessageProperties messageProperties
            , RabbitTemplate rabbitTemplate
            , @Qualifier("apiLogCustomExchange") CustomExchange apiLogCustomExchange
            , @Qualifier("apiLogQueue") Queue apiLogQueue
            , @Qualifier("apiLogBinding") Binding apiLogBinding
            , @Nullable IMessageSendService messageSendService
            , @Nullable AmqpAdmin amqpAdmin) {
        log.info("\r\n==== api 日志将通过 Rabbit MQ 发送到消费服务器！ \r\n====");
        ApiLogSendMQHandler scheduledLogSendMQHandler = new ApiLogSendMQHandler(messageProperties, rabbitTemplate);
        if (messageSendService != null) {
            messageSendService.addHandler(scheduledLogSendMQHandler);
        }
        if (amqpAdmin != null) {
            amqpAdmin.declareExchange(apiLogCustomExchange);
            amqpAdmin.declareQueue(apiLogQueue);
            amqpAdmin.declareBinding(apiLogBinding);
        }
        return scheduledLogSendMQHandler;
    }
}
