package io.github.taybct.tool.core.enums;

import java.io.Serializable;

/**
 * 认证方式枚举
 *
 * @param value 值
 * @param label 名称
 * @author xijieyin <br> 2022/8/5 18:30
 * @since 1.0.0
 */
public record OAuthenticationMethodType(String value, String label) implements Serializable {

    /**
     * 用户名
     */
    public static final OAuthenticationMethodType USERNAME = new OAuthenticationMethodType("username", "用户名");

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        OAuthenticationMethodType that = (OAuthenticationMethodType) obj;
        return this.value().equals(that.value());
    }

    @Override
    public int hashCode() {
        return this.value().hashCode();
    }

}
