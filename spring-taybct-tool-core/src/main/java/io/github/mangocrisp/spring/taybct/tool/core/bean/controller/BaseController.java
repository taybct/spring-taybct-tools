package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.bean.service.IBaseService;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

/**
 * 默认指定了 Long 类型主键的 BaseController,查询类型是实体类自己
 *
 * @author xijieyin <br> 2023/3/15 下午3:16
 */
public interface BaseController<T extends BaseEntity
        , S extends IBaseService<T>>
        extends FullBaseController<T, S, Long, T, T, T> {
    @Override
    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, BaseController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }
}
