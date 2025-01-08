package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.bean.service.IBaseService;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

import java.io.Serializable;

/**
 * 默认指定了查询类型就是实体类的 BaseController,但是主键的类型需要自己指定
 *
 * @author xijieyin <br> 2023/3/15 下午3:19
 */
public interface PKTypeBaseController<T extends BaseEntity
        , S extends IBaseService<T>
        , P extends Serializable>
        extends FullBaseController<T, S, P, T, T, T> {
    @Override
    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, PKTypeBaseController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }
}
