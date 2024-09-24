package io.github.mangocrisp.spring.taybct.tool.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * 返回结果代码
 *
 * @author xijieyin <br> 2022/8/5 18:48
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
@ToString
public final class ResultCode implements IResultCode {
    /**
     * 代码
     */
    private String code;
    /**
     * 消息
     */
    private String message;
    /**
     * HTTP 状态码
     */
    private HttpStatus httpStatus;

    /**
     * 操作成功
     */
    public static final ResultCode OK = new ResultCode("200", "操作成功", HttpStatus.OK);
    /**
     * 客户端认证失败
     */
    public static final ResultCode CLIENT_ERROR = new ResultCode("U403", "客户端认证失败", HttpStatus.FORBIDDEN);
    /**
     * 用户操作异常
     */
    public static final ResultCode USER_ERROR = new ResultCode("U500", "用户操作异常", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * 用户登录异常
     */
    public static final ResultCode USER_LOGIN = new ResultCode("U400", "用户登录异常", HttpStatus.BAD_REQUEST);
    /**
     * 用户不存在
     */
    public static final ResultCode USER_NOT_EXIST = new ResultCode("U404", "用户密码校验失败", HttpStatus.BAD_REQUEST);
    /**
     * 用户登录信息校验失败
     */
    public static final ResultCode USER_PASSWORD_ERROR = new ResultCode("U402", "用户登录信息校验失败", HttpStatus.BAD_REQUEST);
    /**
     * 用户密码校验失败
     */
    public static final ResultCode USER_PASSWORD_INVALIDATED = new ResultCode("U405", "用户密码校验失败", HttpStatus.BAD_REQUEST);
    /**
     * 访问受限
     */
    public static final ResultCode ACCESS_UNAUTHORIZED = new ResultCode("A403", "访问受限", HttpStatus.FORBIDDEN);
    /**
     * 网关异常
     */
    public static final ResultCode GATEWAY_ERROR = new ResultCode("G500", "网关异常", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * Feign 调用异常
     */
    public static final ResultCode FEIGN_ERROR = new ResultCode("FEIGN500", "Feign 调用异常", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * 网关限流：您访问的资源太多人访问了，请稍后再试
     */
    public static final ResultCode GATEWAY_SENTINEL_BLOCK = new ResultCode("GSB0", "网关限流：您访问的资源太多人访问了，请稍后再试", HttpStatus.REQUEST_TIMEOUT);
    /**
     * token已被禁止访问
     */
    public static final ResultCode TOKEN_ACCESS_FORBIDDEN = new ResultCode("T403", "token已被禁止访问", HttpStatus.FORBIDDEN);
    /**
     * token无效或已过期
     */
    public static final ResultCode TOKEN_INVALID_OR_EXPIRED = new ResultCode("T401", "token无效或已过期", HttpStatus.UNAUTHORIZED);
    /**
     * 验证码不能为空
     */
    public static final ResultCode CAPTCHA_VALIDATE_EMPTY = new ResultCode("C400", "验证码不能为空", HttpStatus.BAD_REQUEST);
    /**
     * 验证码不正确或已失效
     */
    public static final ResultCode CAPTCHA_VALIDATE_INVALID = new ResultCode("C400", "验证码不正确或已失效", HttpStatus.BAD_REQUEST);
    /**
     * 验证码不正确
     */
    public static final ResultCode CAPTCHA_VALIDATE_NOT_MATCH = new ResultCode("C400", "验证码不正确", HttpStatus.BAD_REQUEST);
    /**
     * 校验失败
     */
    public static final ResultCode VALIDATE_ERROR = new ResultCode("V400", "校验失败", HttpStatus.BAD_REQUEST);
    /**
     * SQL 异常
     */
    public static final ResultCode SQL_ERROR = new ResultCode("S500", "SQL 异常", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * 数据库操作异常
     */
    public static final ResultCode DATA_ERROR = new ResultCode("D500", "数据库操作异常", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * 基础异常，不要慌，这个异常是人为抛出！
     */
    public static final ResultCode BASE_ERROR = new ResultCode("B500", "基础异常，不要慌，这个异常是人为抛出！", HttpStatus.INTERNAL_SERVER_ERROR);
    /**
     * 未知异常
     */
    public static final ResultCode ERROR = new ResultCode("500", "未知异常", HttpStatus.INTERNAL_SERVER_ERROR);


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        ResultCode that = (ResultCode) obj;
        return this.getCode().equals(that.getCode());
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode();
    }

}