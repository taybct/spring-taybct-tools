package io.github.taybct.tool.core.result;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * 请求返回结果包装类<br>
 * data 是 JSONObject 可以自由操作一些东西
 *
 * @author xijieyin <br> 2022/8/5 18:45
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "请求返回结果包装类")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2794314923397672962L;

    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private String code;
    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;
    /**
     * 返回携带的数据
     */
    @Schema(description = "返回的数据")
    private T data;
    /**
     * 额外的数据
     */
    @Schema(description = "额外数据")
    private JSONObject meta;

    public static <S> R<S> ok() {
        return result(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), null);
    }

    public static <S> R<S> ok(String message) {
        return result(ResultCode.OK.getCode(), message, null);
    }

    public static <S> R<S> ok(String message, S data) {
        return result(ResultCode.OK.getCode(), message, data);
    }

    public static <S> R<S> fail() {
        return result(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    public static <S> R<S> fail(IResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <S> R<S> fail(String code, String message) {
        return result(code, message, null);
    }

    public static <S> R<S> fail(String message) {
        return result(ResultCode.ERROR.getCode(), message, null);
    }

    public static <S> R<S> status(String code, String message) {
        return result(code, message, null);
    }

    public static <S, U extends S> R<S> status(String code, String message, U data) {
        return result(code, message, data);
    }

    public static <S, U extends S> R<S> data(U data) {
        return result(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data);
    }

    private static <S, U extends S> R<S> result(String code, String message, U data) {
        return new R<>(code, message, data, new JSONObject());
    }

    public R<T> put(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }

    /**
     * 是否成功
     *
     * @return Boolean
     * @author xijieyin <br> 2022/8/5 18:46
     * @since 1.0.0
     */
    @Schema(description = "是否成功")
    public Boolean isOk() {
        return this.code.equalsIgnoreCase(ResultCode.OK.getCode());
    }

    /**
     * 是否有返回数据
     *
     * @return Boolean
     * @author xijieyin <br> 2022/8/5 18:46
     * @since 1.0.0
     */
    @Schema(description = "是否有返回数据")
    public Boolean hasData() {
        return Optional.ofNullable(this.data).isPresent();
    }

}
