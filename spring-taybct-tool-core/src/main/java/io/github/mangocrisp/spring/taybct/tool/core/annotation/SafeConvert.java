package io.github.mangocrisp.spring.taybct.tool.core.annotation;


import io.github.mangocrisp.spring.taybct.tool.core.aop.SafeConvertAspect;
import io.github.mangocrisp.spring.taybct.tool.core.enums.EntityType;

import java.io.Serializable;
import java.lang.annotation.*;

/**
 * 安全对象转换
 *
 * @author xijieyin <br> 2022/8/5 13:36
 * @see SafeConvertAspect
 * @see EntityType
 * @since 1.0.0
 */
@Inherited                          //允许子类继承
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SafeConvert {

    /**
     * 指定字段
     */
    String key() default "";

    /**
     * 安全输入的类，填多个类也只取第一个类，就是不管输入什么类型（T），都会转换成这个类型（safeIn）
     * ，但是要注意，要转换的类型（safeIn）必须要是继承输入的类型（T）,或者是确保可以从 T --转换成--&gt; safeOut
     */
    Class<? extends Serializable>[] safeIn() default {};

    /**
     * 安全输出的类，填多个类也只取第一个类，就是不管输出什么类型（T），都会转换成这个类型（safeOut）
     * ，但是要注意，要转换的类型（safeOut）必须要是继承输出的类型（T）,或者是确保可以从 T --转换成--&gt; safeOut
     */
    Class<? extends Serializable>[] safeOut() default {};

    /**
     * 返回类型
     */
    EntityType resultType() default EntityType.NONE;

    /**
     * 忽略输入的字段
     */
    String[] ignoreIn() default {};

    /**
     * 忽略输出的字段
     */
    String[] ignoreOut() default {};

}
