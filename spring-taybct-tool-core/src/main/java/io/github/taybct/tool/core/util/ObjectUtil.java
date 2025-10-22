package io.github.taybct.tool.core.util;


import org.apache.commons.lang3.ObjectUtils;

/**
 * 对象工具类
 *
 * @author xijieyin <br> 2022/8/5 19:48
 * @since 1.0.0
 */
public class ObjectUtil extends ObjectUtils {

    /**
     * 获取值，如果值为空就返回默认值
     *
     * @param value        值
     * @param defaultValue 默认值
     * @return T
     * @author xijieyin <br> 2022/8/5 19:48
     * @since 1.0.0
     */
    public static <T> T getValue(T value, T defaultValue) {
        if (isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

}
