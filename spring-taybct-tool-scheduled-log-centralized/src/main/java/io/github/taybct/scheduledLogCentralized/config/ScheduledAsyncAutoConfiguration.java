package io.github.taybct.scheduledLogCentralized.config;

import io.github.taybct.scheduledLogCentralized.async.ScheduledTaskDecorator;
import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.util.ScheduledVirtualThreadExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class ScheduledAsyncAutoConfiguration implements AsyncConfigurer {

    private final ScheduledLogCollector scheduledLogCollector;

    @Override
    public Executor getAsyncExecutor() {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        // ⚠️ 虚拟线程 Executor 不支持 setTaskDecorator
        // 所以需要包装一层
        return new ScheduledVirtualThreadExecutor(
                executor,
                new ScheduledTaskDecorator(scheduledLogCollector)
        );
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            // 这里可以打底日志（也会进 Scheduled 日志）
        };
    }
}