package io.github.taybct.scheduledLogCentralized.restTemplate;

import io.github.taybct.scheduledLogCentralized.config.ScheduledLogContant;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import io.github.taybct.scheduledLogCentralized.dto.ScheduledRunSnapshot;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 这个拦截类用于resttemplate，所以如果进来时有runid的话，肯定是由定时任务统一入口来的，所以把这个runid作为parentid保证链路畅通
 */
@Component
public class ScheduledRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        ScheduledRunSnapshot snapshot = ScheduledRunContext.snapshot();
        if (snapshot != null) {
            HttpHeaders headers = request.getHeaders();
            headers.add(ScheduledLogContant.FEIGN_MDC_RUN_ID, UUID.randomUUID().toString());
            headers.add(ScheduledLogContant.FEIGN_MDC_MODULE, snapshot.getModule());
            headers.add(ScheduledLogContant.FEIGN_MDC_METHOD, snapshot.getMethod());
            headers.add(ScheduledLogContant.FEIGN_PARENT_RUN_ID, snapshot.getRunId());
        }

        return execution.execute(request, body);
    }
}