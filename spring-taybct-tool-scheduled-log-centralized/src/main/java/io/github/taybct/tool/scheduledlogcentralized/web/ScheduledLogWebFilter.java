package io.github.taybct.tool.scheduledlogcentralized.web;

import io.github.taybct.tool.scheduledlogcentralized.collector.ScheduledLogCollector;
import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogContant;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import io.github.taybct.tool.scheduledlogcentralized.util.ModuleNameUtil;
import io.github.taybct.tool.scheduledlogcentralized.util.ScheduledErrorHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
//feign调用日志归档
@Component
public class ScheduledLogWebFilter implements Filter {

    private final ScheduledLogCollector collector;

    public ScheduledLogWebFilter(ScheduledLogCollector collector) {
        this.collector = collector;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;

        String runId = req.getHeader(ScheduledLogContant.FEIGN_MDC_RUN_ID);
        String module = ModuleNameUtil.getModuleName();
        StringBuffer requestURL = ((HttpServletRequest) request).getRequestURL();
        String parentRunId = req.getHeader(ScheduledLogContant.FEIGN_PARENT_RUN_ID);
        boolean active = runId != null;

        if (active) {
            ScheduledRunContext.start(runId, module, String.valueOf(requestURL),parentRunId);
        }

        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            // 记录未捕获异常
            ScheduledErrorHolder.record(t);
            throw t;
        } finally {
            if (active) {
                collector.collect(runId);
                ScheduledRunContext.clear();
            }
        }
    }
}