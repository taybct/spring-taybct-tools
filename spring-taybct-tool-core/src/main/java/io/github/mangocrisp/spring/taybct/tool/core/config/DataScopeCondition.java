package io.github.mangocrisp.spring.taybct.tool.core.config;

import io.github.mangocrisp.spring.taybct.tool.core.bean.ILoginUser;

import java.util.function.Predicate;

/**
 * 数据权限是否过滤的条件判断
 *
 * @author XiJieYin <br> 2023/6/29 10:27
 */
public interface DataScopeCondition extends Predicate<ILoginUser> {
}
