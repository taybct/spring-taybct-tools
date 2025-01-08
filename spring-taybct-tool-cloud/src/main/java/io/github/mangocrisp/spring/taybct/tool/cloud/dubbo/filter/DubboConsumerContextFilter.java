package io.github.mangocrisp.spring.taybct.tool.cloud.dubbo.filter;

import io.github.mangocrisp.spring.taybct.tool.core.constant.AuthHeaderConstants;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.*;
import org.springframework.util.PatternMatchUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dubbo 服务之间调用的时候传递参数，这个是消费端
 *
 * @author XiJieYin <br> 2023/2/3 0:28
 */
public class DubboConsumerContextFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 把所有的请求头信息（当然，这个有做过滤，一些请求头是不需要的）都传到服务提供者
        HttpServletRequest httpServletRequest = ServletUtil.getRequest();
        if (ObjectUtils.isNotEmpty(httpServletRequest)) {
            Map<String, String> headers = ServletUtil.getHeaders(httpServletRequest);
            Map<String, String> headersFilterMap = new HashMap<>();
            List<String> allowHeadsList = new ArrayList<>(AuthHeaderConstants.ALLOWED);
            headers.keySet().forEach(key -> {
                // 只支持配置的 header
                if (allowHeadsList.contains(key)
                        || PatternMatchUtils.simpleMatch(AuthHeaderConstants.PATTERN, key)
                        || PatternMatchUtils.simpleMatch(AuthHeaderConstants.X_FORWARDED_PATTERN, key)
                ) {
                    String values = headers.get(key);
                    // header value 不为空的 传递
                    if (StringUtils.isNotBlank(values)) {
                        headersFilterMap.put(key, values);
                    }
                }

            });
            // 配置客户端IP
            headersFilterMap.put("X-Forwarded-For", ServletUtil.getIpAddr());
            //设置需要的内容
            RpcContext.getContext().setAttachments(headersFilterMap);
        }
        return invoker.invoke(invocation);
    }

}
