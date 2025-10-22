package io.github.taybct.tool.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * bean 工具类
 *
 * @author XiJieYin <br> 2024/3/23 15:25
 */
public class BeanUtil extends cn.hutool.core.bean.BeanUtil {

    /**
     * 获取接口上的泛型
     *
     * @param o              接口
     * @param interfaceClass 要获取接口泛型的接口的类型
     * @param index          泛型索引
     * @return 类型
     */
    public static <T> Class<T> getInterfaceT(Object o, Class<?> interfaceClass, int index) {
        return Arrays.stream(o.getClass().getGenericInterfaces())
                .filter(t -> t instanceof ParameterizedType)
                .map(t -> (ParameterizedType) t)
                .filter(t -> t.getRawType().getTypeName().equals(interfaceClass.getTypeName()))
                .findFirst()
                .map(t -> (Class<T>) checkType(t, index))
                .orElseThrow(() -> new IllegalArgumentException("找不到指定的接口的类型！"));
    }

    /**
     * 获取类型上的泛型 T
     *
     * @param o     类
     * @param index 泛型索引
     * @return 类型
     */
    public static <T> Class<T> getClassT(Object o, int index) {
        return checkType(o.getClass().getGenericSuperclass(), index);
    }

    private static <T> Class<T> checkType(Type type, int index) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        } else if (type instanceof ParameterizedType pt) {
            Type t = pt.getActualTypeArguments()[index];
            return checkType(t, index);
        } else {
            String className = type == null ? "" : type.getClass().getName();
            throw new IllegalArgumentException("Expected a Class, ParameterizedType, but <" + type + "> is of type " + className);
        }
    }


    /**
     * 获取到所有的字段
     *
     * @param type 类型
     * @return list
     */
    public static List<Field> getAllFields(Class<?> type) {
        return getAllFields(new ArrayList<>(), type);
    }

    /**
     * 获取到所有的字段
     *
     * @param fields 字段列表
     * @param type   类型
     * @return list
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

}
