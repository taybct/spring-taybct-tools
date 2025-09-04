package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import java.lang.annotation.*;

/**
 * <pre>
 * websocket reactive 匹配
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/6/11 14:18
 */
@Inherited                          //允许子类继承
@Target({ElementType.METHOD, ElementType.TYPE})//用于方法和类上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
public @interface ServerReactiveEndpoint {

    String value() default "";

}
