package io.github.mangocrisp.spring.taybct.tool.cloud.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;

/**
 * dubbo 服务之间调用的时候传递参数，这个是服务提供端
 *
 * @author XiJieYin <br> 2023/2/3 0:53
 */
@Slf4j
public class DubboProviderContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        log.trace("获取到请求头参数：");
        RpcContext.getContext().getAttachments().forEach((k, v) -> log.trace("{} ===> {}", k, v));
        return invoker.invoke(invocation);
    }

}
