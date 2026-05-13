package io.github.taybct.tool.scheduledlogcentralized.async;

import io.github.taybct.tool.scheduledlogcentralized.collector.ScheduledLogCollector;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import io.github.taybct.tool.scheduledlogcentralized.dto.ScheduledRunSnapshot;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;
import java.util.UUID;

public class ScheduledTaskDecorator implements TaskDecorator {

    private final ScheduledLogCollector collector;

    public ScheduledTaskDecorator(ScheduledLogCollector collector) {
        this.collector = collector;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        // ⭐ 关键判断：不是 Scheduled 触发的，直接原样返回
        if (!ScheduledRunContext.isActive()) {
            return runnable;
        }
        // 父线程 MDC
        Map<String, String> parentMdc = MDC.getCopyOfContextMap();
        ScheduledRunSnapshot parentSnapshot = ScheduledRunContext.snapshot();

        return () -> {
            Map<String, String> old = MDC.getCopyOfContextMap();

            // ===== 1. 为 @Async 派生新的 runId =====
            String asyncRunId = UUID.randomUUID().toString();

            try {
                // ===== 2. 继承 MDC（如 traceId 等）=====
                if (parentMdc != null) {
                    MDC.setContextMap(parentMdc);
                }

                // ===== 3. 启动新的 Scheduled 上下文 =====
                ScheduledRunContext.start(
                        asyncRunId,
                        parentSnapshot != null ? parentSnapshot.getModule() : null,
                        parentSnapshot != null ? parentSnapshot.getMethod() : null,
                        parentSnapshot != null ? parentSnapshot.getRunId() : null // parentRunId
                );

                // ===== 4. 执行业务 =====
                runnable.run();

            } catch (Throwable ex) {
                // 未捕获异常也写日志
                org.slf4j.LoggerFactory.getLogger("SCHEDULED_ASYNC_ERROR")
                        .error("Async execution error", ex);
                throw ex;
            } finally {

                // ===== 5. async 自己汇总、自己入库 =====
                try {
                    collector.collect(asyncRunId);
                } finally {
                    ScheduledRunContext.clear();
                }

                // ===== 6. 还原 MDC =====
                if (old != null) {
                    MDC.setContextMap(old);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}