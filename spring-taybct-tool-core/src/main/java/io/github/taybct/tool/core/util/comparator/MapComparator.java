package io.github.taybct.tool.core.util.comparator;

import cn.hutool.core.util.ReUtil;
import io.github.taybct.tool.core.util.ObjectUtil;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 过滤器
 */
@Data
public class MapComparator<T> {
    /**
     * 过滤类型
     */
    private FilterType filterType;
    /**
     * 值
     */
    private T condition;
    /**
     * 集合值
     */
    private Set<T> conditionSet;
    /**
     * 范围值
     */
    private List<T> range;

    /**
     * 过滤
     *
     * @param value 需要比较的值
     * @return 比较结果
     */
    public boolean filter(T value) {
        if (filterType != FilterType.IS_NULL && filterType != FilterType.IS_NOT_NULL && ObjectUtil.isEmpty(value)) {
            // 如果不是判断空或者非空，但是值是空就直接返回 false
            return false;
        }
        return switch (filterType) {
            case EQ -> eq(value);
            case NE -> !eq(value);
            case LT -> lt(value);
            case GT -> !lt(value);
            case LE -> le(value);
            case GE -> ge(value);
            case RANGE -> range(value);
            case LIKE -> contains(value);
            case NOT_LIKE -> !contains(value);
            case RIGHT_LIKE -> !startWith(value);
            case LEFT_LIKE -> !endWith(value);
            case IS_NULL -> ObjectUtil.isEmpty(value);
            case IS_NOT_NULL -> ObjectUtil.isNotEmpty(value);
            case REGEX -> regex(value);
            case IN -> in(value);
        };
    }

    /**
     * 值在集合里面
     *
     * @param value 值
     * @return 结果
     */
    public boolean in(T value) {
        return conditionSet.contains(value);
    }

    /**
     * 值相等
     *
     * @param value 值
     * @return 结果
     */
    public boolean eq(T value) {
        return value.equals(condition);
    }

    /**
     * 值比条件小
     *
     * @param value 值
     * @return 结果
     */
    public boolean lt(T value) {
        if (value instanceof Comparable comparable) {
            return comparable.compareTo(condition) < 0 ? true : false;
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 小于等于
     *
     * @param value 值
     * @return 结果
     */
    public boolean le(T value) {
        if (value instanceof Comparable comparable) {
            return comparable.compareTo(condition) <= 0 ? true : false;
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 大于等于
     *
     * @param value 值
     * @return 结果
     */
    public boolean ge(T value) {
        if (value instanceof Comparable comparable) {
            return comparable.compareTo(condition) >= 0 ? true : false;
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 区间
     *
     * @param value 值
     * @return 结果
     */
    public boolean range(T value) {
        if (value instanceof Comparable comparable) {
            return comparable.compareTo(range.get(0)) >= 0 && comparable.compareTo(range.get(1)) <= 0;
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 模糊
     *
     * @param value 值
     * @return 结果
     */
    public boolean contains(T value) {
        if (value instanceof CharSequence charSequence) {
            return StringUtil.contains(charSequence, (CharSequence) condition);
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 以什么开始
     *
     * @param value 值
     * @return 结果
     */
    public boolean startWith(T value) {
        if (value instanceof CharSequence charSequence) {
            return StringUtil.startsWith(charSequence, (CharSequence) condition);
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 以什么结束
     *
     * @param value 值
     * @return 结果
     */
    public boolean endWith(T value) {
        if (value instanceof CharSequence charSequence) {
            return StringUtil.endsWith(charSequence, (CharSequence) condition);
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }

    /**
     * 正则匹配
     *
     * @param value 值
     * @return 结果
     */
    public boolean regex(T value) {
        if (value instanceof CharSequence charSequence) {
            return ReUtil.isMatch((String) condition, charSequence);
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }


}
