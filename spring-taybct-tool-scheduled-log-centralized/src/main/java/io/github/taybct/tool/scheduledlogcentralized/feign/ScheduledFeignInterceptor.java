package io.github.taybct.tool.scheduledlogcentralized.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogContant;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import io.github.taybct.tool.scheduledlogcentralized.dto.ScheduledRunSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnClass({FallbackFactory.class})
public class ScheduledFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ScheduledRunSnapshot snapshot = ScheduledRunContext.snapshot();
        if (snapshot != null) {
            String runId = snapshot.getRunId();
            if (runId != null) {
                template.header(ScheduledLogContant.FEIGN_MDC_RUN_ID, runId);
                template.header(ScheduledLogContant.FEIGN_MDC_MODULE, ScheduledRunContext.snapshot().getModule());
                template.header(ScheduledLogContant.FEIGN_MDC_METHOD, ScheduledRunContext.snapshot().getMethod());
                template.header(ScheduledLogContant.FEIGN_PARENT_RUN_ID, ScheduledRunContext.snapshot().getParentRunId());
            }
        }
    }
}