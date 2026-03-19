package io.github.taybct.scheduledLogCentralized.config;

import feign.RequestInterceptor;
import io.github.taybct.scheduledLogCentralized.feign.ScheduledFeignInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduledLogAutoConfiguration {

    @Bean
    @ConditionalOnClass(RequestInterceptor.class)
    public ScheduledFeignInterceptor scheduledFeignInterceptor() {
        return new ScheduledFeignInterceptor();
    }
}