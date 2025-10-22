package io.github.taybct.tool.launch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * 项目版本号
     */
    @Value("${application.version:\"\"}")
    String applicationVersion;

    @Async
    @Order
    @EventListener(WebServerInitializedEvent.class)
    public void afterStart(WebServerInitializedEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        int localPort = event.getWebServer().getPort();
        String profile = StringUtils.arrayToCommaDelimitedString(environment.getActiveProfiles());
        if (applicationVersion != null && !applicationVersion.isEmpty()) {
            applicationVersion = String.format(" 项目版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m]\r\n", applicationVersion);
        } else {
            applicationVersion = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\33[3m\33[1m\r\n pikaへ　　　　　／|");
        sb.append("\r\n　　/＼7　　　 ∠＿/");
        sb.append("\r\n　 /　│　　 ／　／");
        sb.append("\r\n　 │　Z ＿,＜　／　　 /`ヽ");
        sb.append("\r\n　 │　　　　　ヽ　　 /　　〉");
        sb.append("\r\n　 Y　　　　　 `　 /　　/");
        sb.append("\r\n　ｲ●　､　●　　⊂⊃ 〈　　/");
        sb.append("\r\n　()　 へ　　　　|　＼〈");
        sb.append("\r\n　　>ｰ ､_　 ィ　 │ ／／");
        sb.append("\r\n　 / へ　　 /　ﾉ＜| ＼＼");
        sb.append("\r\n　 ヽ_ﾉ　　(_／　 │／／");
        sb.append("\r\n　　7　　　　　　　|");
        sb.append("\r\n   ＞―r￣￣`ｰ―＿/");
        sb.append("\r\n\33[4m 　                     ");
        System.out.format("%s" +
                        "\r\n\33[0m\33[3m\33[1m%n 模块：[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] 启动完成 " +
                        "\r\n 当前使用的端口:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n 环境变量:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n%s spring-taybct 版本:[\33[32;1m3.5.x\33[0m\33[3m\33[1m] " +
                        "\r\n spring boot 版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n java 版本:[\33[0m\33[32;1m%s\33[0m\33[3m\33[1m] " +
                        "\r\n\33[4m 　                     \r\n\33[0m\33[1m" +
                        "\33[0m\r\n\r\n"
                , sb, appName, localPort, profile, applicationVersion, SpringBootVersion.getVersion(), System.getProperty("java.version"));
    }

}
