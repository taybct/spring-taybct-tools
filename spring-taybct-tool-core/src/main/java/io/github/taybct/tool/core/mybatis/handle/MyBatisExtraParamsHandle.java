package io.github.taybct.tool.core.mybatis.handle;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MyBatis 额外参数处理器
 *
 * @author XiJieYin <br> 2023/7/4 9:41
 */
public interface MyBatisExtraParamsHandle extends BiFunction<MappedStatement, Map<String, Object>, Map<String, Object>> {

    @Override
    default Map<String, Object> apply(MappedStatement mappedStatement, Map<String, Object> stringObjectMap) {
        return stringObjectMap;
    }

    /**
     * {@link StatementHandler#prepare(Connection, Integer)} 操作前置处理
     * <p>
     * 改改sql啥的
     *
     * @param sh                 StatementHandler(可能是代理对象)
     * @param connection         Connection
     * @param transactionTimeout transactionTimeout
     */
    default void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        // do nothing
    }

}
