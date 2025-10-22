package io.github.taybct.tool.core.config;

import io.github.taybct.tool.core.annotation.DataScope;

/**
 * 默认的自定义规则
 *
 * @author XiJieYin <br> 2023/6/30 11:22
 */
public class DefaultDataScopeCustom implements DataScopeCustom {

    @Override
    public String apply(DataScope dataScope) {
        return null;
    }

}
