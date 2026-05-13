package io.github.taybct.tool.scheduledlogcentralized.config;

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

    //按什么模式执行，默认是WHITE_LIST(其他目前有NONE,BLACK_LIST,ALL)
    private ScheduledLogModule module = ScheduledLogModule.WHITE_LIST;

    //使用统一定时任务入口时，哪些任务需要进行日志管理
    private List<String> logCollectionWhiteList = new ArrayList<String>();

    //使用统一定时任务入口时，哪些任务不需要进行日志管理
    private List<String> logCollectionBlackList = new ArrayList<String>();
}
