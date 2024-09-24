package io.github.mangocrisp.spring.taybct.tool.core.exception.handler;

import javax.servlet.ServletRequest;

/**
 * 异常记录器
 *
 * @author xijieyin <br> 2023/1/5 10:01
 */
public interface IGlobalExceptionReporter {

    /**
     * 开始记录全局捕获的异常
     *
     * @param servletRequest 请求
     * @param e              异常
     */
    void recording(ServletRequest servletRequest, Throwable e);

}
