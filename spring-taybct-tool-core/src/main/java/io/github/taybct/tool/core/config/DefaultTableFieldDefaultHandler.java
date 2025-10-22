package io.github.taybct.tool.core.config;

import java.io.Serializable;

/**
 * 默认实现
 *
 * @author XiJieYin <br> 2023/7/3 16:40
 */
public class DefaultTableFieldDefaultHandler implements TableFieldDefaultHandler<Serializable> {

    @Override
    public Serializable get(Object entity) {
        return null;
    }

}
