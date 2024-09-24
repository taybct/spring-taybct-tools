package io.github.mangocrisp.spring.taybct.tool.cloud.annotation;


import io.github.mangocrisp.spring.taybct.tool.cloud.config.FeignAutoConfiguration;
import io.github.mangocrisp.spring.taybct.tool.core.config.ApplicationConfig;
import io.github.mangocrisp.spring.taybct.tool.core.constant.AppConstants;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.annotation.*;

/**
 * spring boot 项目 启动需要的一些配置
 *
 * @author xijieyin <br> 2022/8/5 20:06
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 可以让指定的注解在某个类上使用后，这个类的子类也将自动被该注解标记
@Inherited
// 表示通过 aop 框架暴露该代理对象, AopContext 能够访问
@EnableAspectJAutoProxy(exposeProxy = true)
// 开启线程异步执行
@EnableAsync
// nacos 热加载
@RefreshScope
// 自动加载应用配置和 Feign 配置
@Import({ApplicationConfig.class, FeignAutoConfiguration.class})
@EnableDiscoveryClient
// 组合注解，减少 Feign 的 basePackages 配置，大家默认都用 AppConstants.BASE_PACKAGES 就行了
@EnableFeignClients
public @interface TayBctCloudConfig {

    String[] value() default {};

    String[] basePackages() default {AppConstants.BASE_PACKAGES};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};

}
