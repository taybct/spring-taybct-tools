package io.github.taybct.tool.core.enums;

import io.github.taybct.tool.core.annotation.SafeConvert;
import lombok.Getter;

/**
 * 类型转换对象类型，用于 {@linkplain  SafeConvert @SafeConvert }
 *
 * @author xijieyin <br> 2022/8/5 18:24
 * @since 1.0.0
 */
@Getter
public enum EntityType {
    /**
     * 未指定
     */
    NONE,
    /**
     * 实体类
     */
    Entity,
    /**
     * 集合
     */
    Collection,
    /**
     * 分页
     */
    Page
}
