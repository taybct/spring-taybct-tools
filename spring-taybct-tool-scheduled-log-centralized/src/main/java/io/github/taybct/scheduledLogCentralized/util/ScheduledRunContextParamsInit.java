package io.github.taybct.scheduledLogCentralized.util;

import io.github.taybct.scheduledLogCentralized.config.ScheduledLogProperties;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ScheduledRunContextParamsInit {

    public ScheduledRunContextParamsInit(ObjectProvider<ScheduledLogProperties> provider,
                                         ScheduledLogProperties scheduledLogProperties) {
        ScheduledRunContext.setProvider(provider);
        RedisScheduledJobTemplate.setscheduledLogProperties(scheduledLogProperties);
    }
}
