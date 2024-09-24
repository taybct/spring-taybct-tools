package io.github.mangocrisp.spring.taybct.tool.core.exception.handler;

import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 全局捕获输出器，需要怎么输出给请求端
 *
 * @author xijieyin <br> 2023/1/5 10:15
 */
public interface IGlobalPrinter {

    /**
     * 打印输出
     *
     * @param e               异常
     * @param servletResponse 响应
     */
    void print(Throwable e, ServletResponse servletResponse);

    /**
     * 直接输出 R 对象
     *
     * @param response 响应
     * @param r        转换后的 R 对象，成功或者失败都行
     * @throws IOException IO 异常
     */
    void outputResponse(HttpServletResponse response, R<?> r) throws IOException;

}
