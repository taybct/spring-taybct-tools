package io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle;

import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;

import java.util.Collections;
import java.util.Set;

/**
 * 租户 id 提供者
 *
 * @author xijieyin <br> 2022/8/4 17:59
 * @since 1.0.0
 */
public interface ITenantSupplier {
    /**
     * 是否开启多租户
     *
     * @return boolean
     * @author xijieyin <br> 2022/8/26 9:18
     * @since 1.0.2
     */
    default Boolean getEnable() {
        return false;
    }

    /**
     * 提供获取租户 id 的方式
     *
     * @return String
     * @author xijieyin <br> 2022/8/4 18:00
     * @since 1.0.0
     */
    default String getTenantId() {
        throw new BaseException("can not find a default tenant id.");
    }

    /**
     * 获取租户表
     *
     * @return {@code Set<String>}
     * @author xijieyin <br> 2022/8/4 18:00
     * @since 1.0.0
     */
    default Set<String> getTenantTables() {
        return Collections.emptySet();
    }

    /**
     * 获取用于区分租户的字段
     *
     * @return String
     * @author xijieyin <br> 2022/8/4 18:00
     * @since 1.0.0
     */
    default String getTenantIdColumn() {
        return "tenant_id";
    }
}
