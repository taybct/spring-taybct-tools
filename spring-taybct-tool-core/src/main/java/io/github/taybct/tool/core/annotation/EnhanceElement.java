package io.github.taybct.tool.core.annotation;

import java.lang.annotation.*;
import java.util.function.Function;

/**
 * 方法参数/字段等元素需要增强处理
 * <br>
 * 注意,如果需要处理的是集合对象,不要使用 {@code Arrays.asList()} 传参,这样会导致无法调用 {@code collection.clear()} 方法
 * ,你应该是 {@code new ArrayList(Arrays.asList())} 这样使用
 *
 * @author XiJieYin <br> 2024/4/19 14:11
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})//参数和字段上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Inherited                          //允许子类继承
public @interface EnhanceElement {

    /**
     * 如果是放在 {@link EnhanceElements} 注解里面需要指定是哪个字段要操作
     */
    String value() default "";

    /**
     * 如果是放在参数里面就是指定如果这个类型是一个 Map 集合，就是 Map 集合的 key
     */
    EnhanceElementMap[] map() default {};

    /**
     * 参数处理器，作为参数时处理，可以添加多个按顺序处理
     */
    Class<? extends Function<Object, Object>>[] parameterHandler() default {};

    /**
     * 结果处理器，作为返回结果时处理，可以添加多个按顺序执行处理
     */
    Class<? extends Function<Object, Object>>[] resultHandler() default {};

}
