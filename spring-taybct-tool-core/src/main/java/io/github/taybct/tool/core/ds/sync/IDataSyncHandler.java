package io.github.taybct.tool.core.ds.sync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.function.BiFunction;

/**
 * <pre>
 * 数据同步接口
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/4/14 17:19
 */
public interface IDataSyncHandler {

    /**
     * 用户查询源数据的 statement
     *
     * @param conn   数据库连接
     * @param config 配置
     * @return statement
     */
    PreparedStatement queryStatement(Connection conn, DataSyncConfig config);

    /**
     * 查询所有的数据，使用游标，读一条，同步一条
     * 先根据 同步记录表里面记录的上一次更新的时候来判断是否有最新的更新记录
     * 1、如果同步记录表里面没有这个表的记录，说明就从来没同步过，需要把所有的都同步一遍
     * 2、如果同步记录表里面有这个表的记录了就是按上一次同步的时候来判断 ，如果表里面的更新记录有大于上一次同步的，就同步这些比上一次同步时间要大的数据
     * 使用游标读取记录，读一条写一条
     *
     * @param config 配置
     */
    void sync(DataSyncConfig config
            , BiFunction<Connection, DataSyncConfig, PreparedStatement> queryStatement);

}
