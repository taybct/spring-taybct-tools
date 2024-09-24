package io.github.mangocrisp.spring.taybct.tool.core.handle;

import io.github.mangocrisp.spring.taybct.tool.core.config.TableFieldDefaultHandler;

import java.io.Serializable;

/**
 * 默认主键值设置
 *
 * @author XiJieYin <br> 2024/4/18 14:49
 */
public interface TableFieldDefaultUKHandler extends TableFieldDefaultHandler<Serializable> {

    @Override
    default Serializable get() {
        return 0L;
    }
}
