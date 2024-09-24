package io.github.mangocrisp.spring.taybct.tool.launch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;

/**
 * 项目启动监听
 *
 * @author xijieyin <br> 2022/8/5 20:08
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
public class StartEventListener {

    @Async
    @Order
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(WebServerInitializedEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        int localPort = event.getWebServer().getPort();
        String profile = StringUtils.arrayToCommaDelimitedString(environment.getActiveProfiles());
        String applicationVersion = environment.getProperty("application.version");
        if (applicationVersion != null && !applicationVersion.isEmpty()) {
            applicationVersion = String.format(" 项目版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m]\r\n", applicationVersion);
        } else {
            applicationVersion = "";
        }
        String sb = "\33[3m\33[1m\r\n {\\____/}" +
                "\r\n ( ´◔ ‸◔`)" +
                "\r\n /つ启动成功~" +
                "\r\n\33[4m 　                     ";
        System.out.format("%s" +
                        "\r\n\33[0m\33[3m\33[1m%n 模块：[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] 启动完成 " +
                        "\r\n 当前使用的端口:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n 环境变量:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n%s spring-taybct 版本:[\33[32;1m2.7.x\33[0m\33[3m\33[1m] " +
                        "\r\n spring boot 版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n java 版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n\33[4m 　                     \r\n\33[0m\33[1m" +
                        "\33[0m\r\n\r\n"
                , sb, appName, localPort, profile, applicationVersion, SpringBootVersion.getVersion(), System.getProperty("java.version"));
    }

}
