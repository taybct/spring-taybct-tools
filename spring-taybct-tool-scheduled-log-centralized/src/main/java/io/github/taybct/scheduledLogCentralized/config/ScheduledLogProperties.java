package io.github.taybct.scheduledLogCentralized.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties("scheduled-log-centralized")
@Component
@Getter
@Setter
@RefreshScope
@Configuration
public class ScheduledLogProperties {
    private boolean enabled;
}
