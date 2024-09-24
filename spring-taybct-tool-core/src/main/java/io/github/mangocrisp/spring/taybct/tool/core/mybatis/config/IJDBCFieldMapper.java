package io.github.mangocrisp.spring.taybct.tool.core.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * <pre>
 * JDBC 字段匹配
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/9/7 02:27
 */
public interface IJDBCFieldMapper {
    /**
     * Java 字段转 数据库字段
     *
     * @return 转换之后的类型转换方法匹配
     */
    default ConcurrentHashMap<Class<?>, BiFunction<DbType, Object, String>> j2d(){
        return new ConcurrentHashMap<>();
    }
    /**
     * 数据库字段 转 Java 字段
     *
     * @return 转换之后的类型转换方法匹配
     */
    default ConcurrentHashMap<String, BiFunction<DbType, Object, Class<?>>> d2j(){
        return new ConcurrentHashMap<>();
    }

}
