package io.github.mangocrisp.spring.taybct.tool.core.mybatis.interceptor;

import cn.hutool.core.util.ArrayUtil;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.handle.MyBatisExtraParamsHandle;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MyBatis 额外参数拦截器，可以添加一些额外参数到，用来方便做动态 SQL 查询
 *
 * @author XiJieYin <br> 2023/7/3 15:49
 */
@RequiredArgsConstructor
@Slf4j
@Intercepts(
        {
                @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class MyBatisExtraParamsInterceptor implements Interceptor {

    private List<MyBatisExtraParamsHandle> myBatisExtraParamsHandle = new ArrayList<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        if (target instanceof Executor) {
            if (ArrayUtil.isEmpty(args)) {
                return invocation.proceed();
            }
            invocation.getMethod();
            MappedStatement ms = (MappedStatement) args[0];
            Method method = MyBatisUtil.getMethod(ms);
            if (method == null) {
                return invocation.proceed();
            }
            Map<String, Object> paramMap;
            if (MyBatisUtil.checkTableFieldDefault(args[1])) {
                // 如果查检出来需要设置默认值的方法，就会根据注解设置默认值
                paramMap = MyBatisUtil.getParamMap(ms, args[1], true);
            } else {
                paramMap = MyBatisUtil.getParamMap(ms, args[1]);
            }
            this.myBatisExtraParamsHandle.forEach(handle -> args[1] = handle.apply(ms, paramMap));
        } else {
            StatementHandler sh = (StatementHandler) target;
            Connection connections = (Connection) args[0];
            Integer transactionTimeout = (Integer) args[1];
            this.myBatisExtraParamsHandle.forEach(handle -> handle.beforePrepare(sh, connections, transactionTimeout));
        }
        return invocation.proceed();
    }

    /**
     * 添加处理器
     *
     * @param handler 处理器
     */
    public void addHandler(MyBatisExtraParamsHandle handler) {
        this.myBatisExtraParamsHandle.add(handler);
    }

}
