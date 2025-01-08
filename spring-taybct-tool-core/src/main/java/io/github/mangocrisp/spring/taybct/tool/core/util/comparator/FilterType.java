package io.github.mangocrisp.spring.taybct.tool.core.util.comparator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 过滤类型
 */
@AllArgsConstructor
@Getter
public enum FilterType {
    /**
     * 值等于
     */
    EQ,
    /**
     * 值不等于
     */
    NE,
    /**
     * 小于
     */
    LT,
    /**
     * 大于
     */
    GT,
    /**
     * 小于等于
     */
    LE,
    /**
     * 大于等于
     */
    GE,
    /**
     * 区间（可以是数字或者是日期）
     */
    RANGE,
    /**
     * 模糊包含
     */
    LIKE,
    /**
     * 不包含
     */
    NOT_LIKE,
    /**
     * 以什么开始
     */
    RIGHT_LIKE,
    /**
     * 以什么结束
     */
    LEFT_LIKE,
    /**
     * 是空
     */
    IS_NULL,
    /**
     * 不是空
     */
    IS_NOT_NULL,
    /**
     * 正则匹配
     */
    REGEX,
    /**
     * 在集合
     */
    IN,
}
