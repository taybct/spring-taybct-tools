package io.github.mangocrisp.spring.taybct.tool.core.exception.def;

import io.github.mangocrisp.spring.taybct.tool.core.result.IResultCode;
import io.github.mangocrisp.spring.taybct.tool.core.result.ResultCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.io.Serial;

/**
 * 基础异常
 *
 * @author xijieyin <br> 2022/8/5 18:39
 * @since 1.0.0
 */
public class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 722090751111307193L;

    /**
     * http 状态码
     */
    @Getter
    private HttpStatus httpStatus;
    /**
     * 业务代码
     */
    @Getter
    private String code;
    /**
     * 报错信息
     */
    private String message;
    /**
     * 报错
     */
    private Throwable cause;

    public BaseException() {
        super();
        this.message = ResultCode.BASE_ERROR.getMessage();
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.code = ResultCode.BASE_ERROR.getCode();
    }

    public BaseException(@NonNull String message, @NonNull HttpStatus httpStatus, String code, Throwable cause) {
        super();
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.cause = cause;
    }

    public BaseException(@NonNull String message, @NonNull HttpStatus httpStatus, String code) {
        super();
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public BaseException(@NonNull String message, @NonNull HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
        this.code = ResultCode.BASE_ERROR.getCode();
        this.message = message;
    }

    public BaseException(@NonNull HttpStatus httpStatus) {
        super();
        this.httpStatus = httpStatus;
        this.code = ResultCode.BASE_ERROR.getCode();
        this.message = ResultCode.BASE_ERROR.getMessage();
    }

    public BaseException(@NonNull String message) {
        super();
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.code = ResultCode.BASE_ERROR.getCode();
        this.message = message;
    }

    public BaseException(@NonNull IResultCode resultCode) {
        super();
        this.httpStatus = resultCode.getHttpStatus();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BaseException(@NonNull IResultCode resultCode, @NonNull String message) {
        super();
        this.httpStatus = resultCode.getHttpStatus();
        this.code = resultCode.getCode();
        this.message = message;
    }

    public BaseException(@NonNull String message, String code) {
        super();
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.message = message;
        this.code = code;
    }

    public BaseException(@NonNull String message, Throwable cause) {
        super();
        this.message = message;
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.code = ResultCode.BASE_ERROR.getCode();
        this.cause = cause;
    }

    public BaseException(@NonNull Throwable cause) {
        super();
        this.message = ResultCode.BASE_ERROR.getMessage();
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.code = ResultCode.BASE_ERROR.getCode();
        this.cause = cause;
    }

    public BaseException(@NonNull Throwable cause, String format, Object... args) {
        super();
        this.message = String.format(format, args);
        this.httpStatus = ResultCode.BASE_ERROR.getHttpStatus();
        this.code = ResultCode.BASE_ERROR.getCode();
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public synchronized Throwable getCause() {
        return this.cause;
    }

    public BaseException setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public BaseException setCode(String code) {
        this.code = code;
        return this;
    }

    public BaseException setMessage(String message) {
        this.message = message;
        return this;
    }

    public BaseException setMessage(String format, Object... args) {
        this.message = String.format(format, args);
        return this;
    }

    public BaseException setCause(Throwable cause) {
        this.cause = cause;
        return this;
    }
}
