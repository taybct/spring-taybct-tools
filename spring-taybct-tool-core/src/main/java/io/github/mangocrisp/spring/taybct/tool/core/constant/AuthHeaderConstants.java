package io.github.mangocrisp.spring.taybct.tool.core.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 鉴权请求头常量
 *
 * @author xijieyin <br> 2022/8/5 18:15
 * @since 1.0.0
 */
public interface AuthHeaderConstants {

    /**
     * RestTemplate 和 Fegin 透传到下层的 Headers 名称表达式 以 taybct-* 开头的是系统传的参数
     */
    String PATTERN = "taybct-*";

    /**
     * 记录请求的源头
     */
    String X_FORWARDED_PATTERN = "x-forwarded-*";

    /**
     * RestTemplate 和 Fegin 透传到下层的 Headers 名称列表
     */
    List<String> ALLOWED = Arrays.asList("X-Real-IP", "forwarded", "authorization", "referer", "Authorization", "payload", "Knife4jApi");

    /**
     * 认证请求头key
     */
    String AUTHORIZATION_KEY = "Authorization";

    /**
     * JWT令牌前缀
     */
    String JWT_PREFIX = "Bearer ";


    /**
     * Basic认证前缀
     */
    String BASIC_PREFIX = "Basic ";

    /**
     * 用户名
     */
    String USER_NAME_KEY = "username";

    /**
     * 密码
     */
    String PASSWORD_KEY = "password";

    /**
     * 客户端 id
     */
    String CLIENT_ID_KEY = "client_id";

    /**
     * 授权类型
     */
    String GRANT_TYPE_KEY = "grant_type";

    /**
     * 刷新 token
     */
    String REFRESH_TOKEN_KEY = "refresh_token";

    /**
     * 域
     */
    String SCOPE_KEY = "scope";

}
