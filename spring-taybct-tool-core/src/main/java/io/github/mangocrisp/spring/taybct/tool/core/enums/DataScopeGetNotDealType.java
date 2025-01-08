package io.github.mangocrisp.spring.taybct.tool.core.enums;

/**
 * 如果获取不到权限，或者查询不到权限的处理方式，这个一般用于使用 MyBatisPlus 的方法去查询的时候，如果是使用自定义的 SQL，这个就全靠开发人员自己决定怎么使用这个
 * 查询
 *
 * @author XiJieYin <br> 2023/7/6 11:34
 */
public enum DataScopeGetNotDealType {
    /**
     * 允许为空，也就是乐观处理方式，如果没有分配，说明
     */
    ALLOW,
    /**
     * 不允许
     */
    FORBID
}
