package io.github.taybct.tool.core.sms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信配置类型
 *
 * @author xijieyin
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum ConfigType {
    /**
     * 注册
     */
    REGISTER("register"),
    /**
     * 注册通过
     */
    REGISTERED("registered"),
    /**
     * 登录
     */
    LOGIN("login");
    final String type;
}
