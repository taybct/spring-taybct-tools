package io.github.mangocrisp.spring.taybct.tool.core.constant;

import cn.hutool.core.convert.Convert;

import java.util.Optional;

/**
 * 系统参数获取接口
 *
 * @author xijieyin <br> 2022/12/6 16:52
 * @since 2.0.2
 */
public interface ISysParamsObtainService {


    /**
     * 获取参数对象
     *
     * @param key 键
     * @return 值
     */
    Object getObject(String key);

    /**
     * 获取参数对象
     *
     * @param key    键
     * @param tClass 目标类型
     * @return 值
     */
    default <T> T getObject(String key, Class<T> tClass) {
        return getObject(key, tClass, null);
    }

    /**
     * 获取参数对象
     *
     * @param key          键
     * @param tClass       目标类型
     * @param defaultValue 默认值
     * @return 值
     */
    default <T> T getObject(String key, Class<T> tClass, T defaultValue) {
        return Optional.ofNullable(getObject(key))
                .map(o -> Convert.convert(tClass, o, defaultValue))
                .orElse(defaultValue);
    }

    /**
     * 获取系统参数
     *
     * @param key 键
     * @return String 值
     */
    default String get(String key) {
        return get(key, null);
    }

    /**
     * 获取系统参数
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return String 值
     */
    default String get(String key, String defaultValue) {
        return getObject(key, String.class, defaultValue);
    }

}
