package io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableLogicUnique;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
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
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 逻辑删除额外处理
 *
 * @author XiJieYin <br> 2024/4/18 20:31
 */
@RequiredArgsConstructor
@Slf4j
public class UniqueDeleteLogicExtraHandle implements MyBatisExtraParamsHandle {

    final Environment env;

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
                // 官方说明中很重要的一点是逻辑删除"只对自动注入的 sql 起效"，也就是说XML中自定义的SQL不会自动拼接逻辑删除条件也不会将物理删除更改为逻辑删除
                ParameterMap parameterMap = ms.getParameterMap();
                Class<?> cls = parameterMap.getType();
                if (!cls.isAnnotationPresent(TableLogicUnique.class)) {
                    MyBatisExtraParamsHandle.super.beforePrepare(sh, connection, transactionTimeout);
                    return;
                }
                List<Field> fields = new ArrayList<>();
                BeanUtil.getAllFields(fields, cls);
                Method[] methods = cls.getMethods();
                // 主键
                String pkKey = null;
                // 唯一逻辑删除键
                String logicDeleteUniqueColumnName = Optional.ofNullable(env.getProperty("mybatis-plus.global-config.db-config.logic-delete-unique-field"))
                        .orElse("unique_key");
                if (cls.isAnnotationPresent(TableLogicUnique.class)) {
                    TableLogicUnique annotation = cls.getAnnotation(TableLogicUnique.class);
                    if (StringUtil.isNotBlank(annotation.columnName())) {
                        logicDeleteUniqueColumnName = annotation.columnName();
                    }
                }
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        // 如果是静态字段，就不管
                        continue;
                    }
                    if (field.isAnnotationPresent(TableId.class)) {
                        pkKey = field.getName();
                        TableId annotation = field.getAnnotation(TableId.class);
                        if (StringUtil.isNotBlank(annotation.value())) {
                            pkKey = annotation.value();
                        }
                    }
                }
                if (StringUtil.isBlank(pkKey)) {
                    throw new BaseException("删除需要指定实体类主键，使用注解：TableId！");
                }

                // 数据库连接信息
                BoundSql boundSql = sh.getBoundSql();
                // 获取到原始sql语句
                String sql = boundSql.getSql();
                log.trace("SQL：{}", sql);

                // 增强sql
                // 通过反射，拦截方法上带有自定义@SqlPermission，并增强sql
                //离谱的是之前的反射无法生效，不知道为什么这个可以生效有待研究
                String mSql = sql;
                String whereStr = " WHERE ";
                // 找到 第一个 where 关键字位置
                int whereIndex = sql.indexOf(whereStr);
                if (whereIndex == -1) {
                    List<String> arrayListByString = StringUtil.getArrayListByString(whereStr);
                    for (String s : arrayListByString) {
                        whereIndex = sql.indexOf(s);
                        if (whereIndex > 0) {
                            whereStr = s;
                            break;
                        }
                    }
                }
                // 找到的 where 语句之后在 where 语句之前拼接一个 set
                if (whereIndex > 0) {
                    mSql = sql.replaceFirst(whereStr, String.format(",%s=%s %s", logicDeleteUniqueColumnName, pkKey, whereStr));
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
