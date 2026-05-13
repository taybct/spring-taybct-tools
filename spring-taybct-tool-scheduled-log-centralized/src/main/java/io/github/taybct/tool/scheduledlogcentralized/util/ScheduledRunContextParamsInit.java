package io.github.taybct.tool.scheduledlogcentralized.util;

import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogProperties;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
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
