package io.github.taybct.tool.scheduledlogcentralized.config;

import feign.RequestInterceptor;
import io.github.taybct.tool.scheduledlogcentralized.feign.ScheduledFeignInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RequestInterceptor.class)
public class ScheduledLogAutoConfiguration {

    @Bean
    public ScheduledFeignInterceptor scheduledFeignInterceptor() {
        return new ScheduledFeignInterceptor();
    }
}