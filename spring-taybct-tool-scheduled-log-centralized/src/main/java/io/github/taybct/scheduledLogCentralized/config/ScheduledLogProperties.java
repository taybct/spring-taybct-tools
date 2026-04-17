package io.github.taybct.scheduledLogCentralized.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("scheduled-log-centralized")
@Component
@Getter
@Setter
@RefreshScope
@Configuration
public class ScheduledLogProperties {
    //是否开启
    private boolean enabled;

    //使用统一定时任务入口时，哪些任务需要进行日志管理
    private List<String> logCollectionTask = new ArrayList<String>();
}
