package io.github.taybct.tool.core.annotation;

import java.lang.annotation.*;
import java.util.function.Function;

/**
 * 如果参数或者返回结果是 map 对象，针对 map 里面的每个 key 值进行不同的处理
 *
 * @author XiJieYin <br> 2024/6/7 14:03
 */
@Target({ElementType.ANNOTATION_TYPE})//参数和字段上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Inherited                          //允许子类继承
public @interface EnhanceElementMap {

    /**
     * 如果是放在 {@link EnhanceElements} 注解里面需要指定是哪个字段要操作
     */
    String[] value() default {};

    /**
     * 参数处理器，作为参数时处理，可以添加多个按顺序处理
     */
    Class<? extends Function<Object, Object>>[] parameterHandler() default {};

    /**
     * 结果处理器，作为返回结果时处理，可以添加多个按顺序执行处理
     */
    Class<? extends Function<Object, Object>>[] resultHandler() default {};

}
