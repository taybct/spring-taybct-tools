package io.github.mangocrisp.spring.taybct.tool.cloud.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.mangocrisp.spring.taybct.tool.core.constant.AuthHeaderConstants;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.PatternMatchUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * feign 请求拦截器
 *
 * @author xijieyin <br> 2022/8/5 20:08
 * @since 1.0.0
 */
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        HttpServletRequest httpServletRequest = ServletUtil.getRequest();
        if (ObjectUtils.isNotEmpty(httpServletRequest)) {
            Map<String, String> headers = ServletUtil.getHeaders(httpServletRequest);

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
                        requestTemplate.header(key, values);
                    }
                }

            });
            // 配置客户端IP
            requestTemplate.header("X-Forwarded-For", ServletUtil.getIpAddr());
        }
    }
}
