package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import io.github.mangocrisp.spring.taybct.tool.core.enums.SimpleDBOperateType;
import io.github.mangocrisp.spring.taybct.tool.core.handle.DefaultQueryConditionHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.QueryConditionHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.SyncToAnywhereHandler;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <pre>
 * 将对数据库的单表的简单的增删改查操作同步到任何处，可以是 NoSQL，可以是文件，甚至可以是其他第三方的接口，这些也全都可以自定义如何去实现
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/13 14:34
 */
@Target({ElementType.METHOD})//用于方法上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Inherited                          //允许子类继承
public @interface SyncToAnywhere {

    /**
     * 需要操作的数据，默认拿第一个参数
     *
     * @return 数据名
     */
    String value() default "";

    /**
     * 需要操作的数据，默认拿第一个参数
     *
     * @return 数据名
     */
    @AliasFor("value")
    String dto() default "";

    /**
     * 查询参数，默认拿第一个参数
     *
     * @return 数据名
     */
    @AliasFor("value")
    String params() default "";

    /**
     * 查询分页参数，默认拿第二个参数
     *
     * @return 数据名
     */
    String page() default "";

    /**
     * 数据库操作类型
     *
     * @return 数据库操作类型
     */
    SimpleDBOperateType type() default SimpleDBOperateType.UNKNOWN;

    /**
     * 需要转换的数据类型，和处理器的数据顺序一致
     *
     * @return 类型
     */
    Class<?>[] convert() default {};

    /**
     * 处理器，如果 type 是 SELECT，只拿第一个
     *
     * @return 处理器
     */
    Class<? extends SyncToAnywhereHandler>[] handler() default {};

    /**
     * 查询条件处理器，不同的数据源查询条件不同
     *
     * @return 处理器
     */
    Class<? extends QueryConditionHandler> queryConditionHandler() default DefaultQueryConditionHandler.class;

    /**
     * 查询的数据的类型，如果是查询接口，这个就必须要指定，不然就按原本的接口查询
     *
     * @return 类型
     */
    Class<?> queryDataClass() default Void.class;

    /**
     * 返回结果类型转换，查询回来的结果类型，不指定就是按原接口类型
     *
     * @return 类型
     */
    Class<?> queryResultConverter() default Void.class;

    /**
     * 是否异步操作
     *
     * @return 是否
     */
    boolean executeAsync() default true;

}
