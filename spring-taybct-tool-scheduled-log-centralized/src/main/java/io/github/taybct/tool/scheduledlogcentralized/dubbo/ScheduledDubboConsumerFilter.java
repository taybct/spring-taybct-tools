package io.github.taybct.tool.scheduledlogcentralized.dubbo;

import io.github.taybct.tool.scheduledlogcentralized.config.ScheduledLogContant;
import io.github.taybct.tool.scheduledlogcentralized.context.ScheduledRunContext;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.config.ConfigCenterConfig;
import org.apache.dubbo.rpc.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

@Activate(group = CommonConstants.CONSUMER)
@ConditionalOnClass(ConfigCenterConfig.class)
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