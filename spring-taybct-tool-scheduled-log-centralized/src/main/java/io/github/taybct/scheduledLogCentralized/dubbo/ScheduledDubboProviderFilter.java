package io.github.taybct.scheduledLogCentralized.dubbo;

import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.config.ScheduledLogContant;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import io.github.taybct.scheduledLogCentralized.util.ScheduledErrorHolder;
import io.github.taybct.tool.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.PROVIDER)
@Slf4j
public class ScheduledDubboProviderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        String runId = RpcContext.getServerAttachment()
                .getAttachment(ScheduledLogContant.DUBBO_MDC_RUN_ID);

        if (runId == null) {
            return invoker.invoke(invocation);
        }

        try {
            ScheduledRunContext.start(
                    runId,
                    RpcContext.getServerAttachment()
                            .getAttachment(ScheduledLogContant.DUBBO_MDC_MODULE),
                    RpcContext.getServerAttachment()
                            .getAttachment(ScheduledLogContant.DUBBO_MDC_METHOD)
            );
            return invoker.invoke(invocation);
        } catch (Throwable t) {
            ScheduledErrorHolder.record(t);
            throw t;
        } finally {
            ScheduledLogCollector collector =
                    SpringUtil.getBean(ScheduledLogCollector.class);
            if (collector != null) {
                collector.collect(runId);
                ScheduledRunContext.clear();
            }else {
                ScheduledRunContext.clear();
                log.error("dubbo日志搜集，获取scheduledLogCollector为null！");
            }
        }
    }
}