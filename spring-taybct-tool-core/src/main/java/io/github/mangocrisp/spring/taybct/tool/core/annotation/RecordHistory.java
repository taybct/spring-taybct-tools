package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import io.github.mangocrisp.spring.taybct.tool.core.enums.DataOperateType;

import java.lang.annotation.*;
import java.sql.Types;

/**
 * 这个注释用来记录一些表的历史操作记录，这个不同于操作日志，而是把整条数据记录起来，方便后面回滚，或者记录/追责
 *
 * @author XiJieYin <br> 2023/1/29 15:33
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
public @interface RecordHistory {

    /**
     * 指定要记录的对象,如果不指定，默认就会拿第一个参数
     *
     * @return 对象名
     */
    String value() default "";

    /**
     * 类型，数据库对象实体类
     *
     * @return 对象的类型
     */
    Class<?> clazz();

    /**
     * 表名，如果不指定就会默认根据 MybatisPlus 的注释
     * {@linkplain com.baomidou.mybatisplus.annotation.TableName @TableName} 去获取表名
     *
     * @return 表名
     */
    String tableName() default "";

    /**
     * 主键名，这里默认是 id，如果找不到 id 也会根据 MybatisPlus 的注释
     * {@linkplain com.baomidou.mybatisplus.annotation.TableId @TableId}去获取主键
     *
     * @return 主键名
     */
    String primaryKey() default "id";

    /**
     * 主键数据库类型
     *
     * @return Types
     * @see Types
     */
    int pkTypes() default Types.BIGINT;

    /**
     * 操作类型
     *
     * @return 操作类型
     * @see DataOperateType
     */
    int operateType() default DataOperateType.UPDATE;

    /**
     * 多数据源，数据源
     * <br>如果不指定就会拿当前默认的数据源来操作，或者，如果有使用多数据源的注解
     * {@linkplain com.baomidou.dynamic.datasource.annotation.DS @DS} 来改变也当前方法的数据源，这里就会默认使用
     * {@linkplain com.baomidou.dynamic.datasource.annotation.DS @DS} 注解设置的数据源来操作数据
     * <br> 如果指定了就以这个指定的值优先级最高，所以优先级顺序大概是:
     * <br> {@code 默认数据源 < @DS < dataSource}
     *
     * @return 数据源
     */
    String dataSource() default "";

    /**
     * 将要把历史记录保存到哪个表
     *
     * @return 表名
     */
    String historyTableName() default "sys_history_record";

}
