package io.github.mangocrisp.spring.taybct.tool.core.util.comparator;

import cn.hutool.core.util.ReUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
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
        switch (filterType) {
            case EQ:
                return eq(value);
            case NE:
                return !eq(value);
            case LT:
                return lt(value);
            case GT:
                return !lt(value);
            case LE:
                return le(value);
            case GE:
                return ge(value);
            case RANGE:
                return range(value);
            case LIKE:
                return contains(value);
            case NOT_LIKE:
                return !contains(value);
            case RIGHT_LIKE:
                return !startWith(value);
            case LEFT_LIKE:
                return !endWith(value);
            case IS_NULL:
                return ObjectUtil.isEmpty(value);
            case IS_NOT_NULL:
                return ObjectUtil.isNotEmpty(value);
            case REGEX:
                return regex(value);
            case IN:
                return in(value);
            default:
                throw new IllegalArgumentException();
        }
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
        if (value instanceof Comparable) {
            Comparable comparable = (Comparable) value;
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
        if (value instanceof Comparable) {
            Comparable comparable = (Comparable) value;
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
        if (value instanceof Comparable) {
            Comparable comparable = (Comparable) value;
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
        if (value instanceof Comparable) {
            Comparable comparable = (Comparable) value;
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
        if (value instanceof CharSequence) {
            CharSequence charSequence = (CharSequence) value;
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
        if (value instanceof CharSequence) {
            CharSequence charSequence = (CharSequence) value;
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
        if (value instanceof CharSequence) {
            CharSequence charSequence = (CharSequence) value;
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
        if (value instanceof CharSequence) {
            CharSequence charSequence = (CharSequence) value;
            return ReUtil.isMatch((String) condition, charSequence);
        }
        throw new RuntimeException("不支持的数据类型：" + value.getClass());
    }


}
