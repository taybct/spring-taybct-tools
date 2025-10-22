package io.github.taybct.tool.core.annotation;

import io.github.taybct.tool.core.aop.WebLogAspect;

import java.lang.annotation.*;

/**
 * 加上这个注解,才会打印请求头日志
 *
 * @author xijieyin <br> 2022/8/5 13:38
 * @see WebLogAspect
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
public @interface WebLog {
}
