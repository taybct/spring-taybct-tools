package io.github.taybct.scheduledLogCentralized.dubbo;

import io.github.taybct.scheduledLogCentralized.config.ScheduledLogContant;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate(group = CommonConstants.CONSUMER)
public class ScheduledDubboConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if (ScheduledRunContext.isActive()) {
            RpcContext.getClientAttachment().setAttachment(
                    ScheduledLogContant.DUBBO_MDC_RUN_ID,
                    ScheduledRunContext.snapshot().getRunId()
            );
            RpcContext.getClientAttachment().setAttachment(
                    ScheduledLogContant.DUBBO_MDC_MODULE,
                    ScheduledRunContext.snapshot().getModule()
            );
            RpcContext.getClientAttachment().setAttachment(
                    ScheduledLogContant.DUBBO_MDC_METHOD,
                    ScheduledRunContext.snapshot().getMethod()
            );
            RpcContext.getClientAttachment().setAttachment(
                    ScheduledLogContant.DUBBO_PARENT_RUN_ID,
                    ScheduledRunContext.snapshot().getParentRunId()
            );
        }

        return invoker.invoke(invocation);
    }
}