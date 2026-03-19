package io.github.taybct.scheduledLogCentralized.context;


import io.github.taybct.scheduledLogCentralized.config.ScheduledLogContant;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogProperties;
import io.github.taybct.scheduledLogCentralized.dto.ScheduledRunSnapshot;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;

public final class ScheduledRunContext {

    private static final ThreadLocal<ScheduledRunSnapshot> HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<>();

    private static ObjectProvider<ScheduledLogProperties> propertiesProvider;
    public static void setProvider(ObjectProvider<ScheduledLogProperties> provider) {
        ScheduledRunContext.propertiesProvider = provider;
    }

    public static boolean isLogEnabled() {
        ScheduledLogProperties props = propertiesProvider.getIfAvailable();
        return props != null && props.isEnabled();
    }

    public static void start(String runId, String module, String method) {
        if (ScheduledRunContext.isLogEnabled()){
            ScheduledRunSnapshot snapshot = new ScheduledRunSnapshot(runId, module, method);

            HOLDER.set(snapshot);
            ACTIVE.set(Boolean.TRUE);
            // ★★★ 同步到 MDC ★★★
            MDC.put(ScheduledLogContant.MDC_RUN_ID, runId);
            MDC.put(ScheduledLogContant.MDC_MODULE, module);
            MDC.put(ScheduledLogContant.MDC_METHOD, method);
        }
    }

    public static void start(String runId, String module, String method,String parentRunId) {
        if (ScheduledRunContext.isLogEnabled()){
            ScheduledRunSnapshot snapshot =
                    new ScheduledRunSnapshot(runId, module, method, parentRunId);
            HOLDER.set(snapshot);
            ACTIVE.set(Boolean.TRUE);
            // ★★★ 同步到 MDC ★★★
            MDC.put(ScheduledLogContant.MDC_RUN_ID, runId);
            MDC.put(ScheduledLogContant.MDC_MODULE, module);
            MDC.put(ScheduledLogContant.MDC_METHOD, method);
            if (parentRunId != null) {
                MDC.put(ScheduledLogContant.MDC_PARENT_RUN_ID, parentRunId);
            }
        }
    }

    public static ScheduledRunSnapshot snapshot() {
        return HOLDER.get();
    }

    public static void restore(ScheduledRunSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        HOLDER.set(snapshot);
        ACTIVE.set(Boolean.TRUE);
        // ★★★ 恢复 MDC ★★★
        MDC.put(ScheduledLogContant.MDC_RUN_ID, snapshot.getRunId());
        MDC.put(ScheduledLogContant.MDC_MODULE, snapshot.getModule());
        MDC.put(ScheduledLogContant.MDC_METHOD, snapshot.getMethod());
        MDC.put(ScheduledLogContant.MDC_PARENT_RUN_ID, snapshot.getParentRunId());
    }

    public static void clear() {
        HOLDER.remove();
        ACTIVE.remove();
        // ★★★ 清理 MDC ★★★
        MDC.remove(ScheduledLogContant.MDC_RUN_ID);
        MDC.remove(ScheduledLogContant.MDC_MODULE);
        MDC.remove(ScheduledLogContant.MDC_METHOD);
        MDC.remove(ScheduledLogContant.MDC_PARENT_RUN_ID);
    }
    public static boolean isActive() {
        return Boolean.TRUE.equals(ACTIVE.get());
    }
}