package io.github.taybct.tool.core.annotation;

import java.lang.annotation.*;

/**
 * 表字段逻辑处理注解（逻辑删除且，且有唯一索引限制）
 *
 * @author XiJieYin
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface TableLogicUnique {

    /**
     * 数据库表字段名
     */
    String columnName() default "unique_key";

    /**
     * 用于标识唯一逻辑删除的字段
     */
    String value() default "uniqueKey";


}
