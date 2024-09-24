package io.github.mangocrisp.spring.taybct.tool.cloud.config;

import feign.RequestInterceptor;
import io.github.mangocrisp.spring.taybct.tool.cloud.interceptor.FeignRequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Feign 配置注册
 *
 * @author xijieyin <br> 2022/8/5 20:07
 * @since 1.0.0
 */
@AutoConfiguration
public class FeignAutoConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor();
    }

    @Bean
    public feign.Logger.Level multipartLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

}
