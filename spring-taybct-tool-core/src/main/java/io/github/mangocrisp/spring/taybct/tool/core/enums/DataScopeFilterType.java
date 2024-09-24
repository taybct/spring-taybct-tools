package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.Getter;

/**
 * 数据权限过滤类型
 *
 * @author XiJieYin <br> 2023/6/20 18:07
 */
@Getter
public enum DataScopeFilterType {
    /**
     * 按用户
     */
    USER,
    /**
     * 按角色
     */
    ROLE,
    /**
     * 一起过滤取合集
     */
    BOTH
}
