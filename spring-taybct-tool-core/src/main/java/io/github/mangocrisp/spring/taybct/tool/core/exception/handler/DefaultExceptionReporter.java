package io.github.mangocrisp.spring.taybct.tool.core.exception.handler;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xijieyin <br> 2023/1/5 10:06
 */
@Slf4j
public class DefaultExceptionReporter implements IGlobalExceptionReporter {

    @Override
    public void recording(ServletRequest servletRequest, Throwable e) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.error("\r\n======>请求异常：{}:{}\r\n========>{}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
    }

}
