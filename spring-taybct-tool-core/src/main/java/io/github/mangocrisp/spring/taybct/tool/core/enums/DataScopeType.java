package io.github.mangocrisp.spring.taybct.tool.core.enums;

import lombok.Getter;

/**
 * 数据权限类型
 *
 * @author XiJieYin <br> 2023/6/20 17:01
 */
@Getter
public enum DataScopeType {
    /**
     * 一对一的数据权限，例如一个用户只能是某个部门的
     */
    SINGLE,
    /**
     * 一对多的数据权限，例如一个用户可以同时在多个部门
     */
    MULTI
}
