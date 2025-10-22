package io.github.taybct.tool.core.annotation;

import io.github.taybct.tool.core.enhance.IMethodEnhanceHandler;

import java.lang.annotation.*;

/**
 * 方法增强
 * <br>
 * 在方法调用的时候对方法里面的输入参数和输出结果做一些处理
 *
 * @author XiJieYin <br> 2024/4/19 11:41
 */
@Target({ElementType.METHOD})//用于方法上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Inherited                          //允许子类继承
public @interface EnhanceMethod {

    /**
     * 处理器（bean）
     *
     * @return 可以有多个处理器
     */
    Class<? extends IMethodEnhanceHandler>[] handler() default {};

}