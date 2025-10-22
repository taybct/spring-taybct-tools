package io.github.taybct.tool.core.constant;

/**
 * token 常量
 *
 * @author XiJieYin <br> 2023/5/23 15:47
 */
public interface TokenConstants {

    /**
     * 认证方式
     */
    String AUTHENTICATION_METHOD = "atm";

    /**
     * 用户 id
     */
    String USER_ID_KEY = "uid";

    /**
     * 租户 id
     */
    String TENANT_ID_KEY = "tni";

    /**
     * 用户名
     */
    String USER_NAME_KEY = "user_name";

    /**
     * JWT存储权限属性
     */
    String JWT_AUTHORITIES_KEY = "authorities";

    /**
     * JWT存储权限前缀
     */
    String AUTHORITY_PREFIX = "ROLE_";

    /**
     * 域
     */
    String SCOPE_KEY = "scope";

    /**
     * JWT载体key
     */
    String JWT_PAYLOAD_KEY = "payload";

    /**
     * JWT ID 唯一标识
     */
    String JWT_JTI = "jti";

    /**
     * jwt token 过期时间
     */
    String JWT_EXP = "exp";

    /**
     * 客户端 id
     */
    String CLIENT_ID_KEY = "client_id";

    /**
     * 主体
     */
    String PRINCIPAL = "principal";

    /**
     * 凭据
     */
    String CREDENTIALS = "credentials";

}
