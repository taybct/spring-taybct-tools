package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import io.github.mangocrisp.spring.taybct.tool.core.config.DefaultTableFieldDefaultHandler;
import io.github.mangocrisp.spring.taybct.tool.core.config.TableFieldDefaultHandler;
import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.*;

/**
 * 数据库做新增操作的时候，设置数据数据的默认值，这个虽然可以在数据库设置，但是每个数据库的数据类型是有差异的 <br>
 * 但是 Java 代码的类型是不变的，所以这里直接在 Java 里面设置，就可以不用在数据库设置了，数据库也好兼容和复制!!! <br>
 *
 * @author xijieyin <br> 2022/10/25 11:17
 * @since 1.0.5
 */
@Inherited                          //允许子类继承
@Documented
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)    //生命周期，有SOURCE（源码）、CLASS（编译）、RUNTIME（运行时）
public @interface TableFieldDefault {

    /**
     * 值
     */
    String value() default "";

    /**
     * 是否设置为空字符串
     */
    boolean isBlank() default false;

    /**
     * 是否设置为当前时间
     */
    boolean isTimeNow() default false;

    /**
     * 是否随机，这里默认随机是 uuid
     */
    boolean isRandom() default false;

    /**
     * Spring Expression Language (SpEL) expression. 用来生成默认值
     *
     * @return string
     */
    String expression() default "";

    /**
     * 自定义默认值该如何设置
     *
     * @return TableFieldDefaultHandler
     */
    Class<? extends TableFieldDefaultHandler> handler() default DefaultTableFieldDefaultHandler.class;

    /**
     * 字段自动填充策略
     * <br>
     * 默认只在新增插入操作的时候自动填充
     *
     * @return 可以选择多个 FieldDefaultFill
     */
    SqlCommandType[] fill() default {SqlCommandType.INSERT};

}
