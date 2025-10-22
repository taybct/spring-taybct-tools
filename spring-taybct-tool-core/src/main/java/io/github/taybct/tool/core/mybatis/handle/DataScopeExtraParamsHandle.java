package io.github.taybct.tool.core.mybatis.handle;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import io.github.taybct.tool.core.annotation.DataScope;
import io.github.taybct.tool.core.bean.ILoginUser;
import io.github.taybct.tool.core.config.DataScopeCondition;
import io.github.taybct.tool.core.config.DataScopeCustom;
import io.github.taybct.tool.core.mybatis.config.DataScopeProperties;
import io.github.taybct.tool.core.mybatis.interceptor.DataScopeData;
import io.github.taybct.tool.core.mybatis.util.DataScopeUtil;
import io.github.taybct.tool.core.util.MyBatisUtil;
import io.github.taybct.tool.core.util.SpringUtil;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.beans.BeansException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 数据权限额外参数扩展
 *
 * @author XiJieYin <br> 2023/6/20 14:29
 */
@RequiredArgsConstructor
@Slf4j
public class DataScopeExtraParamsHandle implements MyBatisExtraParamsHandle {

    /**
     * 当前登录的用户
     */
    final Supplier<ILoginUser> userSupplier;

    /**
     * 配置全局参数
     */
    final DataScopeProperties properties;

    /**
     * 权限过滤的条件判断，是否要进行权限过滤
     */
    final DataScopeCondition dataScopeCondition;

    /**
     * 自定义 sql 规则
     */
    final DataScopeCustom dataScopeCustom;

    @SneakyThrows
    @Override
    public Map<String, Object> apply(MappedStatement ms, Map<String, Object> stringObjectMap) {
        Method method = MyBatisUtil.getMethod(ms);
        // 获取到注解
        DataScope dataScope = null;
        if (method != null) {
            dataScope = method.getAnnotation(DataScope.class);
        }
        if (dataScope == null || ignore(dataScope)) {
            // 如果判断条件不满足就真的返回
            return stringObjectMap;
        }

        // 先默认拿注解上的自定义过滤规则，然后再拿全局配置的规则，最后使用默认的规则
        DataScope finalDataScope = dataScope;
        String conditionSql = Optional.ofNullable(getBean(dataScope.custom()).apply(dataScope))
                .orElseGet(() -> Optional.ofNullable(dataScopeCustom.apply(finalDataScope))
                        .orElseGet(() -> DataScopeUtil.init(properties, userSupplier)
                                .getConditionSql(new DataScopeData(finalDataScope), getDbType(ms, finalDataScope))));
        stringObjectMap.put(dataScope.sqlField(), conditionSql);
        return stringObjectMap;
    }

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
        // 获取到注解
        DataScope dataScope = null;
        if (method != null) {
            dataScope = method.getAnnotation(DataScope.class);
        }
        if (dataScope == null || ignore(dataScope) || !dataScope.auto()) {
            // 如果判断条件不满足就真的返回
            MyBatisExtraParamsHandle.super.beforePrepare(sh, connection, transactionTimeout);
            return;
        }
        // 先默认拿注解上的自定义过滤规则，然后再拿全局配置的规则，最后使用默认的规则
        DataScope finalDataScope = dataScope;
        String conditionSql = Optional.ofNullable(getBean(dataScope.custom()).apply(dataScope))
                .orElseGet(() -> Optional.ofNullable(dataScopeCustom.apply(finalDataScope))
                        .orElseGet(() -> DataScopeUtil.init(properties, userSupplier)
                                .getConditionSql(new DataScopeData(finalDataScope), getDbType(ms, finalDataScope))));

        // id为执行的mapper方法的全路径名，如com.cq.UserMapper.insertUser， 便于后续使用反射
        String id = ms.getId();
        // sql语句类型 select、delete、insert、update
        SqlCommandType sct = ms.getSqlCommandType();
        // 数据库连接信息

        BoundSql boundSql = sh.getBoundSql();
        // 获取到原始sql语句
        String sql = boundSql.getSql();
        log.trace("SQL：{}", sql);

        // 增强sql
        // 通过反射，拦截方法上带有自定义@SqlPermission，并增强sql
        //离谱的是之前的反射无法生效，不知道为什么这个可以生效有待研究
        String mSql = "";
        String whereStr = "WHERE";
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
        if (whereIndex > 0) {
            mSql = sql.replaceFirst(whereStr, String.format("%s exists(%s) AND ", whereStr, conditionSql));
        } else {
            mSql = String.format("%s WHERE exists(%s)", sql, conditionSql);
        }
        // 直接增强sql
        //通过反射修改sql语句
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, mSql);
        log.trace("增强后的SQL：{}", mSql); // 打印：增强后的SQL
        MyBatisExtraParamsHandle.super.beforePrepare(sh, connection, transactionTimeout);
    }

    private static DbType getDbType(MappedStatement ms, DataScope dataScope) {
        DbType d = DbType.getDbType(ms.getConfiguration().getDatabaseId());
        return d.equals(DbType.OTHER) ? dataScope.dbtype() : d;
    }

    /**
     * 判断是否要忽略权限过滤
     *
     * @param dataScope 注解
     * @return boolean
     * @throws InstantiationException 异常
     * @throws IllegalAccessException 异常
     */
    private boolean ignore(DataScope dataScope) throws InstantiationException, IllegalAccessException {
        return !getBean(dataScope.dataScopeCondition()).test(userSupplier.get())
                || !dataScopeCondition.test(userSupplier.get());
    }

    /**
     * 获取 bean
     *
     * @param clazz 类型
     * @param <T>   类型
     * @return 类型对象
     * @throws InstantiationException 报错
     * @throws IllegalAccessException 报错
     */
    private <T> T getBean(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        try {
            return SpringUtil.getBean(clazz);
        } catch (BeansException e) {
            log.trace("找不到 bean", e);
        }
        try {
            Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            return declaredConstructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException e) {
            log.trace("找不到 bean", e);
            throw new RuntimeException(e);
        }
    }

}
