package io.github.mangocrisp.spring.taybct.tool.core.bean.controller;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ModelConvertible;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;

/**
 * Long 类型 key 请求模型转换 Controller
 *
 * @param <T>  实体类
 * @param <S>  Service
 * @param <QM> 查询模型
 * @param <AM> 新增模型
 * @param <UM> 删除模型
 */
public interface LongKeyConvertibleController<T
        , S extends IService<T>
        , QM extends ModelConvertible<? extends T>
        , AM extends ModelConvertible<? extends T>
        , UM extends ModelConvertible<? extends T>>
        extends ModelConvertibleController<T
        , S
        , Long
        , QM
        , AM
        , UM> {

    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, LongKeyConvertibleController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }

    default Class<T> getEntityClass() {
        return BeanUtil.getInterfaceT(this, LongKeyConvertibleController.class, 0);
    }
}
