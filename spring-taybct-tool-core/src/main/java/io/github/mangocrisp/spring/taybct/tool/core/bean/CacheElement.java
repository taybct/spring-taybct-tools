package io.github.mangocrisp.spring.taybct.tool.core.bean;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 缓存需要的元素
 *
 * @author XiJieYin <br> 2023/2/8 0:53
 */
public final class CacheElement {
    private final String key;
    private final Object value;
    private final long timeout;
    private final TimeUnit unit;

    /**
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 超时时间
     * @param unit    时间单位
     */
    public CacheElement(String key, Object value, long timeout, TimeUnit unit) {
        this.key = key;
        this.value = value;
        this.timeout = timeout;
        this.unit = unit;
    }

    public String key() {
        return key;
    }

    public Object value() {
        return value;
    }

    public long timeout() {
        return timeout;
    }

    public TimeUnit unit() {
        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CacheElement that = (CacheElement) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.value, that.value) &&
                this.timeout == that.timeout &&
                Objects.equals(this.unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, timeout, unit);
    }

    @Override
    public String toString() {
        return "CacheElement[" +
                "key=" + key + ", " +
                "value=" + value + ", " +
                "timeout=" + timeout + ", " +
                "unit=" + unit + ']';
    }

}
