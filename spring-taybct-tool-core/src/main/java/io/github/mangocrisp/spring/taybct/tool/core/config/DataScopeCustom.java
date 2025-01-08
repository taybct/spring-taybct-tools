package io.github.mangocrisp.spring.taybct.tool.core.config;

import io.github.mangocrisp.spring.taybct.tool.core.annotation.DataScope;

import java.util.function.Function;

/**
 * 自定义数据权限过滤
 *
 * @author XiJieYin <br> 2023/6/30 10:51
 */
public interface DataScopeCustom extends Function<DataScope, String> {
}
