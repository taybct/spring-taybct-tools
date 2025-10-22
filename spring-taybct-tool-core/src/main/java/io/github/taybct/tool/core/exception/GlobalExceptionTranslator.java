package io.github.taybct.tool.core.exception;

import io.github.taybct.tool.core.exception.handler.IGlobalExceptionReporter;
import io.github.taybct.tool.core.exception.handler.IGlobalPrinter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 全局异常过滤器
 *
 * @author xijieyin <br> 2023/1/5 9:52
 */
//@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionTranslator {

    /**
     * 记录请求的异常
     */
    final IGlobalExceptionReporter globalExceptionReporter;

    /**
     * 把异常如何抛出
     */
    final IGlobalPrinter globalExceptionPrinter;

    @ExceptionHandler(Throwable.class)
    public void UnknownException(Throwable e
            , HttpServletRequest request
            , HttpServletResponse response) {
        // 记录日志
        globalExceptionReporter.recording(request, e);
        // 抛出异常
        globalExceptionPrinter.print(e, response);
    }

}
