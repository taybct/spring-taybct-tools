package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangocrisp.spring.taybct.tool.core.bean.OriginalModel;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

/**
 * Long 类型主键的控制器
 *
 * @param <T> 实体类类型
 * @param <S> Service 类型
 */
public interface LongKeyOriginalController<T, S extends IService<T>>
        extends ModelConvertibleController<T
        , S
        , Long
        , OriginalModel<T>
        , OriginalModel<T>
        , OriginalModel<T>> {

    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, LongKeyOriginalController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }

    default Class<T> getEntityClass() {
        return BeanUtil.getInterfaceT(this, LongKeyOriginalController.class, 0);
    }
}
