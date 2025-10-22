package io.github.taybct.tool.core.result;

import org.springframework.http.HttpStatus;

/**
 * 返回结果代码接口，具体有哪些代码，请查看：{@link ResultCode}
 *
 * @author xijieyin <br> 2022/8/5 18:45
 * @since 1.0.0
 */
public interface IResultCode {

    String getCode();

    String getMessage();

    HttpStatus getHttpStatus();

}
