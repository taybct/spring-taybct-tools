package io.github.taybct.scheduledLogCentralized.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogContant;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
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
        String runId = ScheduledRunContext.snapshot().getRunId();
        if (runId != null) {
            template.header(ScheduledLogContant.FEIGN_MDC_RUN_ID, runId);
            template.header(ScheduledLogContant.FEIGN_MDC_MODULE, ScheduledRunContext.snapshot().getModule());
            template.header(ScheduledLogContant.FEIGN_MDC_METHOD, ScheduledRunContext.snapshot().getMethod());
            template.header(ScheduledLogContant.FEIGN_PARENT_RUN_ID, ScheduledRunContext.snapshot().getParentRunId());
        }
    }
}