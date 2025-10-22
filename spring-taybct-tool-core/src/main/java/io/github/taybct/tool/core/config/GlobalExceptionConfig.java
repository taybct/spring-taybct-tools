package io.github.taybct.tool.core.config;

import io.github.taybct.tool.core.constant.PropertiesPrefixConstants;
import io.github.taybct.tool.core.exception.def.BaseException;
import io.github.taybct.tool.core.result.R;
import io.github.taybct.tool.core.result.ResultCode;
import io.github.taybct.tool.core.util.StringUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 异常信息配置
 *
 * @author xijieyin <br> 2022/9/23 10:42
 * @since 1.0.4
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = PropertiesPrefixConstants.GLOBAL_EXCEPTION)
public class GlobalExceptionConfig implements BiFunction<Throwable, HttpServletResponse, R<?>> {

    /**
     * 配置
     */
    private List<ExceptionMessage> config = Collections.emptyList();

    @Override
    public R<?> apply(Throwable exception, HttpServletResponse response) {
        return this.getConfig().stream()
                .filter(c -> {
                    try {
                        return Class.forName(c.getClazz()).isAssignableFrom(exception.getClass()) ||
                                (exception.getCause() != null && Class.forName(c.getClazz()).isAssignableFrom(exception.getCause().getClass()));
                    } catch (ClassNotFoundException e) {
                        // 找不到的类就不要了
                        return false;
                    }
                })
                .findFirst()
                .map(ce -> {
                    if (response != null) {
                        response.setStatus(ce.getHttpStatus().value());
                    }
                    // 如果没有指定消息，就直接用报错的消息
                    return StringUtil.isEmpty(ce.getMessage()) ?
                            R.fail(ce.getCode(), exception.getMessage()) :
                            R.fail(ce.getCode(), ce.getMessage());
                })
                .orElseGet(() -> {
                    if (response != null) {
                        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    }
                    if (exception instanceof BaseException cause) {
                        if (response != null) {
                            response.setStatus(cause.getHttpStatus().value());
                        }
                        return R.fail(cause.getCode(), cause.getMessage());
                    }
                    if (exception.getCause() != null && exception.getCause() instanceof BaseException cause) {
                        if (response != null) {
                            response.setStatus(cause.getHttpStatus().value());
                        }
                        return R.fail(cause.getCode(), cause.getMessage());
                    }
                    return R.fail(exception.getMessage());
                });
    }

    @Data
    @NoArgsConstructor(force = true)
    public static class ExceptionMessage {
        /**
         * 异常名
         **/
        private String name;
        /**
         * 异常类型
         */
        @NonNull
        private String clazz;
        /**
         * 异常代码
         **/
        @NonNull
        private String code = ResultCode.ERROR.getCode();
        /**
         * 异常提示
         */
        private String message;
        /**
         * Http 状态码指定，默认是服务器异常
         *
         * @see HttpStatus
         */
        private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
