package io.github.mangocrisp.spring.taybct.tool.core.ds.sync;

import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.handler.DataSyncOracleHandler;
import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.handler.DataSyncPGSQL$MySQLHandler;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <pre>
 * 数据同步工具
 * </pre>
 *
 * @author xijieyin
 * @since 2025/4/14 16:31
 */
@Slf4j
public class DataSyncService {

    /**
     * 根据目标数据库（使用的数据库驱动），匹配不同数据库对应的处理器
     *
     * @see DriverType
     */
    @Getter
    private static final ConcurrentMap<String, IDataSyncHandler> handlerMap = new ConcurrentHashMap<>();

    /**
     * 添加处理器
     *
     * @param driverClassName 驱动类名
     * @param service         处理器
     * @see IDataSyncHandler
     */
    public void addService(String driverClassName, IDataSyncHandler service) {
        assert !StringUtil.isEmpty(driverClassName) && service != null;
        handlerMap.put(driverClassName, service);
    }

    static {
        // oracle 的处理器
        handlerMap.put(DriverType.oracle6andBefore, new DataSyncOracleHandler());
        handlerMap.put(DriverType.oracle7AndThen, new DataSyncOracleHandler());
        handlerMap.put(DriverType.mysql, new DataSyncPGSQL$MySQLHandler());
        handlerMap.put(DriverType.mysqlCJ, new DataSyncPGSQL$MySQLHandler());
        handlerMap.put(DriverType.postgresSQL, new DataSyncPGSQL$MySQLHandler());
    }

    /**
     * 查询所有的数据，使用游标，读一条，同步一条
     * 先根据 同步记录表里面记录的上一次更新的时候来判断是否有最新的更新记录
     * 1、如果同步记录表里面没有这个表的记录，说明就从来没同步过，需要把所有的都同步一遍
     * 2、如果同步记录表里面有这个表的记录了就是按上一次同步的时候来判断 ，如果表里面的更新记录有大于上一次同步的，就同步这些比上一次同步时间要大的数据
     * 使用游标读取记录，读一条写一条
     *
     * @param config 配置
     */
    public void sync(DataSyncConfig config) {
        if (StringUtil.isEmpty(config.getSourceDriver())
                || StringUtil.isEmpty(config.getTargetDriver())
                || !handlerMap.containsKey(config.getSourceDriver())
                || !handlerMap.containsKey(config.getTargetDriver())) {
            throw new BaseException("请配置正确的驱动程序");
        }
        if (StringUtil.isEmpty(config.getSourceUrl())
                || StringUtil.isEmpty(config.getTargetUrl())
                || StringUtil.isEmpty(config.getSourceUser())
                || StringUtil.isEmpty(config.getTargetUser())
                || StringUtil.isEmpty(config.getSourcePass())
                || StringUtil.isEmpty(config.getTargetPass())) {
            throw new BaseException("请配置正确的数据库连接信息");
        }
        if (StringUtil.isEmpty(config.getTargetTable())
                || StringUtil.isEmpty(config.getSourceTable())
                || StringUtil.isEmpty(config.getSqlSelect())) {
            throw new BaseException("请配置正确的同步表信息");
        }
        long beginTime = System.currentTimeMillis();

        handlerMap.get(config.getTargetDriver()).sync(config
                , (connection, cfg) -> handlerMap.get(config.getSourceDriver()).queryStatement(connection, cfg));

        long time = System.currentTimeMillis() - beginTime;
        log.debug("\033[40;32;0m 同步表：{} -> {} 结束，耗时：{} \t\t\t\033[0m", config.getSourceTable(), config.getTargetTable(), time);
    }

}
