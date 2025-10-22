package io.github.taybct.tool.core.annotation;

import java.lang.annotation.*;

/**
 * 标识这是一个表 JSON 字段，一般在 mysql 或者 pgsql 会有这样的字段，这里加一个注解告知一睛额外参数处理这个字段
 * 转换对应的类型，比如 pgsql 是一定要转换成 PGobject 才能插入的，常规的做法是在实体类里面直接重写 setter，
 * 现在有这个注解之后，额外参数拦截器会帮忙处理这件事
 *
 * @author xijieyin <br> 2022/10/25 11:17
 * @since 2.4.0
 */
@Inherited                          //允许子类继承
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)    //生命周期，有SOURCE（源码）、CLASS（编译）、RUNTIME（运行时）
public @interface TableFieldJSON {
}
