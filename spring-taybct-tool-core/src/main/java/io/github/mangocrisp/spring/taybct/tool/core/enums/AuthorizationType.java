package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.Getter;

/**
 * 鉴权类型
 *
 * @author xijieyin <br> 2022/8/5 18:19
 * @since 1.0.0
 */
@Getter
public enum AuthorizationType {
    /**
     * 不需要鉴权
     */
    NONE,
    /**
     * 普通，Bearer Token 通过在头部放 Authorization -&gt; token 信息
     */
    NORMAL,
    /**
     * OAuth2 密码模式 password
     */
    OAUTH2_PASSWORD,
    /**
     * OAuth2 授权码模式 client_credentials
     */
    OAUTH2_CODE,
    /**
     * OAuth2 客户端模式 client_credentials
     */
    OAUTH2_CLIENT

}
