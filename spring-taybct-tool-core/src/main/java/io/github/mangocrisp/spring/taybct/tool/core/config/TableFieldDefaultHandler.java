package io.github.mangocrisp.spring.taybct.tool.core.config;

import java.util.function.Supplier;

/**
 * 自定义默认值规则
 *
 * @author XiJieYin <br> 2023/7/3 16:32
 */
public interface TableFieldDefaultHandler<T> extends Supplier<T> {
}
