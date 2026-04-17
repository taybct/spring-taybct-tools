package io.github.taybct.scheduledLogCentralized.util;


import cn.hutool.core.collection.CollectionUtil;
import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.config.ApplicationContextHolder;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogProperties;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
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

    public static void executeByWhiteList(String method,
            Runnable business,
            ScheduledLogCollector collector) {
        ScheduledLogProperties properties = ApplicationContextHolder.getBean(ScheduledLogProperties.class);
        if (properties == null) {
            business.run();
        }else {
            List<String> logCollectionTask = properties.getLogCollectionTask();
            boolean enabled = properties.isEnabled();
            //开启日志管理和纳入管理的列表不为空时才开启功能
            if (enabled &&  CollectionUtil.isNotEmpty(logCollectionTask)) {
                int index = method.indexOf("?");
                String pureUrl = (index != -1) ? method.substring(0, index) : method;
                boolean contains = logCollectionTask.contains(pureUrl);
                if (contains) {
                    String runId = UUID.randomUUID().toString();
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
                }else {
                    business.run();
                }
            }else  {
                business.run();
            }
        }
    }
}