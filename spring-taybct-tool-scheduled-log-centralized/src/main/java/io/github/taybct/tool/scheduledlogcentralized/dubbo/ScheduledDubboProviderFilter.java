package io.github.taybct.tool.scheduledlogcentralized.dubbo;

import io.github.taybct.tool.scheduledlogcentralized.collector.ScheduledLogCollector;
import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogContant;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import io.github.taybct.tool.scheduledlogcentralized.util.ScheduledErrorHolder;
import io.github.taybct.tool.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.rpc.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Activate(group = CommonConstants.PROVIDER)
@Slf4j
@ConditionalOnClass(ConfigCenterConfig.class)
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