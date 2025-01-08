package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证方式枚举
 *
 * @author xijieyin <br> 2022/8/5 18:30
 * @since 1.0.0
 */
@AllArgsConstructor
@Getter
public final class OAuthenticationMethodType {

    /**
     * 值
     */
    @Getter
    private final String value;

    /**
     * 名称
     */
    @Getter
    private final String label;

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
        return this.getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

}
