package io.github.taybct.tool.core.config;

import io.github.taybct.tool.core.bean.ILoginUser;

/**
 * 默认的数据权限是否过滤的条件判断
 *
 * @author XiJieYin <br> 2023/6/29 10:27
 */
public class DefaultDataScopeCondition implements DataScopeCondition {
    @Override
    public boolean test(ILoginUser securityUtil) {
        return true;
    }
}
