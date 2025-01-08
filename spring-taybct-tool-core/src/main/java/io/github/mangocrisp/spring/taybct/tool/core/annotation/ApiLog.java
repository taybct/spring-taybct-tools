package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import io.github.mangocrisp.spring.taybct.tool.core.constant.OperateType;

import java.lang.annotation.*;

/**
 * 加上这个注解，就会把需要写入日志记录的请求操作信息记录入数据库
 * ，一般是用在 Controller 的方法上
 *
 * @author xijieyin <br> 2022/8/5 13:34
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
public @interface ApiLog {

    /**
     * 模块名
     */
    String title() default "";

    /**
     * 模块描述
     */
    String description() default "";

    /**
     * 操作类型
     */
    String type() default OperateType.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;

    /**
     * 是否保存返回结果
     */
    boolean isSaveResultData() default true;
}
