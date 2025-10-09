package io.github.mangocrisp.spring.taybct.tool.core.bean;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存需要的元素
 *
 * @param key     缓存键
 * @param value   缓存值
 * @param timeout 超时时间
 * @param unit    时间单位
 * @author XiJieYin <br> 2023/2/8 0:53
 */
public record CacheElement(String key, Object value, long timeout, TimeUnit unit) implements Serializable {
}
