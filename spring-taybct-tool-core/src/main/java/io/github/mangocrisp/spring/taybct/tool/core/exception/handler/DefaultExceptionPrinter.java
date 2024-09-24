package io.github.mangocrisp.spring.taybct.tool.core.exception.handler;

import cn.hutool.json.JSONUtil;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.HttpMessageConverters;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;

import java.io.IOException;
import java.util.function.BiFunction;

/**
 * 默认的异常抛出
 *
 * @author xijieyin <br> 2023/1/5 10:28
 */
@RequiredArgsConstructor
public class DefaultExceptionPrinter implements IGlobalPrinter {

    final BiFunction<Throwable, HttpServletResponse, R<?>> failResultConverter;

    GenericHttpMessageConverter<Object> jsonMessageConverter = HttpMessageConverters.getJsonMessageConverter();

    @SneakyThrows
    @Override
    public void print(Throwable e, ServletResponse servletResponse) {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        outputResponse(response, failResultConverter.apply(e, response));
    }

    public void outputResponse(HttpServletResponse response, R<?> r) throws IOException {
        if (jsonMessageConverter != null) {
            ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
            jsonMessageConverter.write(r, R.class, MediaType.APPLICATION_JSON, httpResponse);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Cache-Control", "no-cache");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().println(JSONUtil.parse(r));
            response.getWriter().flush();
        }
    }

}
