package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author xijieyin <br> 2022/11/1 13:46
 * @since 1.1.0
 */
@Inherited                          //允许子类继承
@Target({ElementType.METHOD, ElementType.TYPE})//用于方法和类上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@AutoConfiguration
public @interface Scheduler {

    /**
     * 这个名字，如果不指定，就会默认用 bean 的 name
     */
    @AliasFor(annotation = AutoConfiguration.class)
    String value() default "";

}
