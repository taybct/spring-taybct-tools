package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.bean.service.IBaseService;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

/**
 * 默认指定了 Long 类型的主键的 BaseController 但是查询类型需要自己指定
 *
 * @author xijieyin <br> 2023/3/15 下午3:19
 */
public interface QueryBaseController<T extends BaseEntity
        , S extends IBaseService<T>
        , Q extends T>
        extends FullBaseController<T, S, Long, Q, T, T> {
    @Override
    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, QueryBaseController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }
}
