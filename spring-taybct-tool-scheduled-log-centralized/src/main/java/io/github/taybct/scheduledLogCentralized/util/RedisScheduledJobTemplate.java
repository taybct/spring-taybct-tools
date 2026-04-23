package io.github.taybct.scheduledLogCentralized.util;


import cn.hutool.core.collection.CollectionUtil;
import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.config.ApplicationContextHolder;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogModule;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogProperties;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;

import java.util.List;
import java.util.UUID;

public final class RedisScheduledJobTemplate {
    private RedisScheduledJobTemplate() {
    }
    private static ScheduledLogProperties scheduledLogProperties;
    public static void setscheduledLogProperties(ScheduledLogProperties scheduledLogProperties) {
        RedisScheduledJobTemplate.scheduledLogProperties = scheduledLogProperties;
    }

    public static void dispatchExecute(String method,
                                       Runnable business,
                                       ScheduledLogCollector collector) {
        ScheduledLogModule module = scheduledLogProperties.getModule();
        switch (module) {
            case ALL:
                execute(method, business, collector);
                break;
            case WHITE_LIST:
                executeByWhiteList(method, business, collector);
                break;
            case BLACK_LIST:
                executeByBlackList(method, business, collector);
                break;
            case NONE:
                originalExecute(method, business, collector);
                break;
            default:
                executeByWhiteList(method, business, collector);
        }
    }


    /**
     * 全部进行日志管理
     * @param method
     * @param business
     * @param collector
     */
    private static void execute(
            String method,
            Runnable business,
            ScheduledLogCollector collector
    ) {
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
    }

    /**
     * 不进行日志管理
     * @param method
     * @param business
     * @param collector
     */
    private static void originalExecute(String method,
                                          Runnable business,
                                          ScheduledLogCollector collector) {
        business.run();
    }

    /**
     * 只对白名单中的进行日志管理
     * @param method
     * @param business
     * @param collector
     */
    private static void executeByWhiteList(String method,
            Runnable business,
            ScheduledLogCollector collector) {
        List<String> whiteList = scheduledLogProperties.getLogCollectionWhiteList();
        boolean enabled = scheduledLogProperties.isEnabled();
        if (enabled &&  CollectionUtil.isNotEmpty(whiteList)) {
            int index = method.indexOf("?");
            String pureUrl = (index != -1) ? method.substring(0, index) : method;
            boolean contains = whiteList.contains(pureUrl);
            if (contains) {
                execute(method, business, collector);
            }else {
                business.run();
            }
        }else  {
            business.run();
        }

    }

    /**
     * 黑名单中的不进行日志管理
     * @param method
     * @param business
     * @param collector
     */
    private static void executeByBlackList(String method,
            Runnable business,
            ScheduledLogCollector collector) {
        List<String> blackList = scheduledLogProperties.getLogCollectionBlackList();
        boolean enabled = scheduledLogProperties.isEnabled();
        if (enabled &&  CollectionUtil.isNotEmpty(blackList)) {
            int index = method.indexOf("?");
            String pureUrl = (index != -1) ? method.substring(0, index) : method;
            boolean contains = blackList.contains(pureUrl);
            if (contains) {
                business.run();
            }else {
                execute(method, business, collector);
            }
        }else  {
            execute(method, business, collector);
        }
    }


}