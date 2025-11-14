package io.github.taybct.tool.core.message;

import io.github.taybct.tool.core.message.impl.MessageSendServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志配置
 *
 * @author xijieyin <br> 2023/2/23 下午4:40
 */
@AutoConfiguration
@Slf4j
@EnableConfigurationProperties({MessageProperties.class})
public class MessageConfig {

    @Bean
    @ConditionalOnMissingBean(name = "apiLogSendServiceTaskExecutor")
    public TaskExecutor apiLogSendServiceTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("apiLogSendServiceTask");
        //executor.setConcurrencyLimit(100); // 最大并发 100 个平台线程
        executor.setVirtualThreads(false); // 是否启用虚拟线程模式（兼容 jdk 21 以下版本，如果能明确使用 jdk 21 可以设置 true 打开虚拟线程）
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean(IMessageSendService.class)
    public IMessageSendService apiLogSendService(MessageProperties apiLogProperties
            , ApplicationContext applicationContext
            , @Qualifier("apiLogSendServiceTaskExecutor") TaskExecutor apiLogSendServiceTaskExecutor) {
        ConcurrentHashMap<MessageType, IMessageSendHandler> messageSendHandlerList = new ConcurrentHashMap<>();
        applicationContext.getBeansOfType(IMessageSendHandler.class).values().forEach(bean -> messageSendHandlerList.put(bean.getMessageType(), bean));
        if (!apiLogProperties.getEnable()) {
            log.info("\r\n\r\n====消息发送 Service 已关闭！=====\r\n\r\n");
        } else {
            log.info("\r\n\r\n====消息发送 Service 已配置！=====\r\n\r\n");
        }
        return new MessageSendServiceImpl(apiLogProperties, messageSendHandlerList, apiLogSendServiceTaskExecutor);
    }

}
