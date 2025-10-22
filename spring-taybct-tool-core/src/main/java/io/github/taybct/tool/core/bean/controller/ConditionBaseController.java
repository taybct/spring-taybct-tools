package io.github.taybct.tool.core.bean.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.taybct.tool.core.annotation.WebLog;
import io.github.taybct.tool.core.bean.BaseEntity;
import io.github.taybct.tool.core.bean.service.IBaseService;
import io.github.taybct.tool.core.mybatisUtil.Condition;
import io.github.taybct.tool.core.result.R;
import io.github.taybct.tool.core.util.BeanUtil;
import io.github.taybct.tool.core.util.SpringUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Map;

/**
 * 分页扩展包装类，P 需要指定主键类型
 *
 * @author jopson <br> 2024-05-17 14:59:10  P extends Serializable
 */
public interface ConditionBaseController<T extends BaseEntity
        , S extends IBaseService<T>
        , P extends Serializable>
        extends FullBaseController<T, S, P, T, T, T> {
    @Override
    default S getBaseService() {
        Class<Object> interfaceT = BeanUtil.getInterfaceT(this, ConditionBaseController.class, 1);
        return (S) SpringUtil.getBean(interfaceT);
    }

    /**
     * @param params 所有参数
     * @return {@code R<IPage < ? extends T>>}
     * @author jopson <br> 2024-05-17 14:51:00
     */
    @Operation(summary = "获取分页")
    @RequestMapping("listPage")
    @WebLog
    default R<IPage<? extends T>> listPage(@RequestParam(required = false) Map<String, Object> params) {
        return R.data(getBaseService().page(Condition.getPage(params), Condition.getQueryWrapper(params, getEntityClass())));
    }


    default Class<T> getEntityClass() {
        return BeanUtil.getInterfaceT(this, ConditionBaseController.class, 0);
    }
}
