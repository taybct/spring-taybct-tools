package io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldDefault;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 逻辑删除额外处理，逻辑删除的时候 MybatisPlus 只设置逻辑删除标识字段为删除状态不会修改其他字段，这里需要把一些默认字段也算上，比如更新的时候需要操作的字段
 *
 * @author XiJieYin <br> 2024/4/18 20:31
 */
@Setter
@Getter
@Slf4j
public class DeleteLogicExtraHandle implements MyBatisExtraParamsHandle {

    /**
     * 指定 Java 的数据该如何转换成 sql 字符串字段值
     * <br>
     * {@code String.format("'%s'", value)} 将会把字段原来的值替换成 {@code 'value'}
     * <br>
     * 当然这个是基于 {@linkplain DeleteLogicExtraHandle#assembleFieldValueFunction} 的默认实现来完成的，你也可以自己去定义如何完成，
     * 大致方向就是对于不同的 Java 类型的字段，如何将值转成数据库认可的
     *
     * @see String#format
     */
    private Map<Class<?>, BiFunction<DbType, Object, String>> assembleFieldValueMap = new ConcurrentHashMap<>();

    /**
     * 具体执行的方法
     */
    private BiFunction<DbType, Object, String> assembleFieldValueFunction = (d, v) -> {
        if (assembleFieldValueMap.containsKey(v.getClass())) {
            return assembleFieldValueMap.get(v.getClass()).apply(d, v);
        }
        return String.format("'%s'", v);
    };

    @SneakyThrows
    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        log.debug("===========beforePrepare================");
        //实际执行的sql是经过层层封装，无法利用简单的一层反射获取到需要使用提供的快捷方法或者对获取到关键数据进行拼装
        MetaObject metaObject = MetaObject.forObject(sh, SystemMetaObject.DEFAULT_OBJECT_FACTORY, SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY,
                new DefaultReflectorFactory());
        // 先拦截到RoutingStatementHandler，里面有个StatementHandler类型的delegate变量，其实现类是BaseStatementHandler，然后就到BaseStatementHandler的成员变量mappedStatement
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        Method method = MyBatisUtil.getMethod(ms);
        // id为执行的mapper方法的全路径名，如com.cq.UserMapper.insertUser， 便于后续使用反射
        String id = ms.getId();
        // 数据库类型
        DbType dbType = DbType.getDbType(ms.getDatabaseId());
        // sql语句类型 select、delete、insert、update
        SqlCommandType sct = ms.getSqlCommandType();
        // 是更新操作
        if (sct.equals(SqlCommandType.UPDATE)) {
            Class<?> declaringClass = method.getDeclaringClass();
            String name = method.getName();
            // 但是执行的却是删除方法就=逻辑删除
            // 删除
            if (declaringClass.isAssignableFrom(BaseMapper.class)
                    && name.startsWith("delete")) {
                // MyBatisPlus 所有的的删除，

                // 数据库连接信息
                BoundSql boundSql = sh.getBoundSql();
                // 获取到原始sql语句
                String sql = boundSql.getSql();
                log.trace("SQL：{}", sql);

                // 增强sql
                // 通过反射，拦截方法上带有自定义@SqlPermission，并增强sql
                //离谱的是之前的反射无法生效，不知道为什么这个可以生效有待研究
                String mSql = sql;
                String setStr = " SET ";
                // 找到 第一个 set 关键字位置
                int whereIndex = sql.indexOf(setStr);
                if (whereIndex == -1) {
                    List<String> arrayListByString = StringUtil.getArrayListByString(setStr);
                    for (String s : arrayListByString) {
                        whereIndex = sql.indexOf(s);
                        if (whereIndex > 0) {
                            setStr = s;
                            break;
                        }
                    }
                }

                // 官方说明中很重要的一点是逻辑删除"只对自动注入的 sql 起效"，也就是说XML中自定义的SQL不会自动拼接逻辑删除条件也不会将物理删除更改为逻辑删除
                ParameterMap parameterMap = ms.getParameterMap();
                Class<?> cls = parameterMap.getType();
                List<Field> fields = new ArrayList<>();
                BeanUtil.getAllFields(fields, cls);
                Method[] methods = cls.getMethods();
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        // 如果是静态字段，就不管
                        continue;
                    }
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(TableFieldDefault.class)) {
                        Object fieldValue = MyBatisUtil.getDefaultValue(null, field, ms);
                        // 这里拿到默认值后，把值设置进去
                        if (ObjectUtil.isNotEmpty(fieldValue)) {
                            String fieldName = field.getName();
                            if (field.isAnnotationPresent(TableField.class)) {
                                // 如果有手动指定是数据库的哪个字段，这里就使用他
                                TableField tableField = field.getAnnotation(TableField.class);
                                fieldName = tableField.value();
                            } else {
                                // 如果没有指定是数据库哪个字段，这里就转驼峰
                                fieldName = StringUtil.humpToUnderline(fieldName);
                            }
                            // 找到的 where 语句之后在 where 语句之前拼接一个 set
                            if (whereIndex > 0) {
                                mSql = mSql.replaceFirst(setStr, String.format("%s%s=%s, ", setStr, fieldName, assembleFieldValueFunction.apply(dbType, fieldValue)));
                            }
                        }
                    }
                }

                // 直接增强sql
                //通过反射修改sql语句
                Field field = boundSql.getClass().getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, mSql);
                log.trace("增强后的SQL：{}", mSql); // 打印：增强后的SQL
            }
        }
        MyBatisExtraParamsHandle.super.beforePrepare(sh, connection, transactionTimeout);
    }
}
