package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import io.github.mangocrisp.spring.taybct.tool.core.constant.AppConstants;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * 接口版本注解
 *
 * @author xijieyin <br> 2022/10/14 15:08
 * @since 1.0.5
 */
@Target({ElementType.METHOD, ElementType.TYPE})//用于方法和类上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Mapping //标识映射
@Inherited                          //允许子类继承
public @interface ApiVersion {
    /**
     * 标识版本号
     */
    int value() default AppConstants.DEFAULT_API_VERSION;//default 表示默认值
}