package io.github.taybct.tool.core.handle;

import io.github.taybct.tool.core.config.TableFieldDefaultHandler;

import java.io.Serializable;

/**
 * 默认主键值设置
 *
 * @author XiJieYin <br> 2024/4/18 14:49
 */
public interface TableFieldDefaultUKHandler extends TableFieldDefaultHandler<Serializable> {

    @Override
    default Serializable get(Object entity) {
        return 0L;
    }
}
