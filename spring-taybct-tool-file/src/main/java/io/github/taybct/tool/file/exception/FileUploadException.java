package io.github.taybct.tool.file.exception;

import java.io.Serial;

/**
 * 文件上传异常
 *
 * @author xijieyin <br> 2022/8/4 16:21
 * @since 1.0.0
 */
public class FileUploadException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7053581713574100569L;

    private Integer code;

    private final String message;

    public FileUploadException(String message) {
        this.message = message;
    }

    public FileUploadException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public FileUploadException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

}
