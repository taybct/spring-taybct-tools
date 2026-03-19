package io.github.taybct.scheduledLogCentralized.util;


import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;

import java.util.UUID;

public final class RedisScheduledJobTemplate {

    public static void execute(
            String method,
            Runnable business,
            ScheduledLogCollector collector
    ) {
        String runId = UUID.randomUUID().toString();
//        String module = environment.getProperty("spring.application.name");
        String module = ModuleNameUtil.getModuleName();

        try {
            ScheduledRunContext.start(runId, module, method);
            business.run();
        } catch (Throwable t) {
            ScheduledErrorHolder.record(t);
            throw t;
        } finally {
            collector.collect(runId);
            ScheduledRunContext.clear();
        }
    }
}