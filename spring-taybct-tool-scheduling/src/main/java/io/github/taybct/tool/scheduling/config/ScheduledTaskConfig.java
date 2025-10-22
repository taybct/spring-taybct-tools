package io.github.taybct.tool.scheduling.config;

import io.github.taybct.tool.scheduling.handle.AbstractTaskSupplier;
import io.github.taybct.tool.scheduling.handle.ITaskSupplier;
import io.github.taybct.tool.scheduling.prop.ScheduledProperties;
import io.github.taybct.tool.scheduling.service.ISchedulingService;
import io.github.taybct.tool.scheduling.service.impl.SchedulingTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 任务调度配置
 *
 * @author xijieyin <br> 2022/10/31 14:48
 * @since 1.1.0
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(ScheduledProperties.class)
public class ScheduledTaskConfig {

    @Bean
    @ConditionalOnMissingBean
    public ITaskSupplier taskSupplier() {
        return new AbstractTaskSupplier() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(ScheduledProperties scheduledProperties) {
        log.debug("创建定时任务调度线程池 start");
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(scheduledProperties.getPoolSize());
        threadPoolTaskScheduler.setThreadNamePrefix(scheduledProperties.getThreadNamePrefix());
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(scheduledProperties.getWaitForTasksToCompleteOnShutdown());
        threadPoolTaskScheduler.setAwaitTerminationSeconds(scheduledProperties.getAwaitTerminationSeconds());
        log.debug("创建定时任务调度线程池 end");
        return threadPoolTaskScheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    public ISchedulingService schedulingService(ITaskSupplier taskSupplier
            , ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        return new SchedulingTaskServiceImpl(taskSupplier
                , threadPoolTaskScheduler
                , taskSupplier.configTaskJob());
    }

}
