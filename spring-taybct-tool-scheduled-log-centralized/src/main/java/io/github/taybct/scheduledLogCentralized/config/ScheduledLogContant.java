package io.github.taybct.scheduledLogCentralized.config;

public interface ScheduledLogContant {
    String MDC_RUN_ID = "scheduledRunId";
    String MDC_MODULE = "scheduledModule";
    String MDC_METHOD = "scheduledMethod";
    String MDC_PARENT_RUN_ID = "scheduledParentRunId";
    String FEIGN_MDC_RUN_ID = "Feign-Run-Id";
    String FEIGN_MDC_MODULE = "Feign-Scheduled-Module";
    String FEIGN_MDC_METHOD = "Feign-Scheduled-Method";
    String FEIGN_PARENT_RUN_ID = "Feign-Scheduled-Parent-Run-Id";
    String DUBBO_MDC_RUN_ID = "Dubbo-Run-Id";
    String DUBBO_MDC_MODULE = "Dubbo-Scheduled-Module";
    String DUBBO_MDC_METHOD = "Dubbo-Scheduled-Method";
    String DUBBO_PARENT_RUN_ID = "Dubbo-Scheduled-Parent-Run-Id";

}
