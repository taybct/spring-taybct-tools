package io.github.mangocrisp.spring.taybct.tool.core.ds.sync.handler;

import io.github.mangocrisp.spring.taybct.tool.core.ds.DBHelper;
import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.DataSyncConfig;
import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.DriverType;
import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.IDataSyncHandler;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * <pre>
 * mysql 和 pgsql 数据同步处理类
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/4/15 10:17
 */
@Slf4j
public class DataSyncPGSQL$MySQLHandler implements IDataSyncHandler {

    /**
     * 单次同步数据量，默认为 512
     */
    private static final int buffer = 512;
    /**
     * 默认的fetchSize，默认为 1000
     */
    private static final int fetchSize = 1000;
    /**
     * 属性获取属性key，默认为 fetchSize
     */
    private static final String fetchSizePropertiesKey = "fetchSize";

    @SneakyThrows
    @Override
    public PreparedStatement queryStatement(Connection conn, DataSyncConfig config) {
        if (StringUtil.isNotEmpty(config.getSourceSchema())) {
            // 如果有手动指定 schema
            conn.setSchema(config.getSourceSchema());
        }
        //原数据源为mysql，则手动使用流的方式同步
        PreparedStatement preparedStatement = conn.prepareStatement(config.getSqlSelect(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if (config.getProperties() != null && config.getProperties().containsKey(fetchSizePropertiesKey)) {
            preparedStatement.setFetchSize(config.getProperties().getInteger(fetchSizePropertiesKey));
        } else if (config.getSourceDriver().equals(DriverType.mysql) || config.getSourceDriver().equals(DriverType.mysqlCJ)) {
            preparedStatement.setFetchSize(Integer.MAX_VALUE);
        } else if (config.getSourceDriver().equals(DriverType.postgresSQL)) {
            preparedStatement.setFetchSize(fetchSize);
        }
        return preparedStatement;
    }

    @SneakyThrows
    @Override
    public void sync(DataSyncConfig config
            , BiFunction<Connection, DataSyncConfig, PreparedStatement> queryStatement) {
        try (Connection sourceConnection = DBHelper.conn(config.getSourceDriver(), config.getSourceUrl(), config.getSourceUser(), config.getSourcePass());
             Connection targetConnection = DBHelper.conn(config.getTargetDriver(), config.getTargetUrl(), config.getTargetUser(), config.getTargetPass());
             PreparedStatement sourceStatement = queryStatement.apply(sourceConnection, config)) {

            String lastSyncTime = "1970-01-01 00:00:00";
            PreparedStatement targetStatement = targetConnection.prepareStatement(config.getSqlLastSyncTime());
            ResultSet targetResultSet = targetStatement.executeQuery();
            while (targetResultSet.next()) {
                Object date = targetResultSet.getDate(config.getFieldLastSyncTime());
                Object time = targetResultSet.getTime(config.getFieldLastSyncTime());
                if (date != null && time != null) {
                    lastSyncTime = date + " " + time;
                    log.debug("\033[40;32;0m 上一次更新时间：{} \t\t\t\033[0m", lastSyncTime);
                    break;
                }
            }
            targetResultSet.close();
            targetStatement.close();

            sourceStatement.setString(1, lastSyncTime);
            try (ResultSet sourceResultSet = sourceStatement.executeQuery()) {
                ResultSetMetaData metaData = sourceResultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                //插入更新语句 开头
                StringBuilder sqlInsertBegin = new StringBuilder();
                sqlInsertBegin.append("insert INTO ").append(config.getTargetTable()).append(" (");
                //结尾(mysql)
                StringBuilder mysqlInsertEnd = new StringBuilder();
                mysqlInsertEnd.append(" ON DUPLICATE KEY UPDATE ");

                //结尾(mysql)
                StringBuilder pgsqlInsertEnd = new StringBuilder();
                pgsqlInsertEnd.append(" ON CONFLICT (").append(config.getFieldUniqueKye()).append(") DO UPDATE SET ");

                // 字段
                StringBuilder fields = new StringBuilder();
                //如果已经存在就更新
                StringBuilder mysqlUpdates = new StringBuilder();
                //如果已经存在就更新
                StringBuilder pgsqlUpdates = new StringBuilder();

                for (int i = 1; i <= columnCount; i++) {
                    String key = metaData.getColumnLabel(i);//列名称
                    fields.append(key);
                    mysqlUpdates.append(key).append("=values(").append(key).append(")");
                    pgsqlUpdates.append(key).append("=EXCLUDED.").append(key);
                    if (i < columnCount) {
                        fields.append(",");
                        mysqlUpdates.append(",");
                        pgsqlUpdates.append(",");
                    }
                }
                sqlInsertBegin.append(fields);
                sqlInsertBegin.append(" ) VALUES");
                //到这里应该是 insert into table (f1,f2,f3...) values

                mysqlInsertEnd.append(mysqlUpdates);
                //到这里应该是 on duplicate key update f1=values(f1),f2=values(f2),f3=values(f3)...

                pgsqlInsertEnd.append(pgsqlUpdates);
                //到这里应该是 ON CONFLICT (id) do update set f1=EXCLUDED.f1,f2=EXCLUDED.f2,f3=EXCLUDED.f3...

                String sqlInsertEnd = switch (config.getTargetDriver()) {
                    case (DriverType.mysql), (DriverType.mysqlCJ) -> mysqlInsertEnd.toString();
                    case (DriverType.postgresSQL) -> pgsqlInsertEnd.toString();
                    default -> throw new RuntimeException("不支持的数据库类型");
                };

                //所有的值
                List<String> vals = new ArrayList<>();

                //统计同步数量
                int count = 0;
                // 4.处理数据库的返回结果(使用ResultSet类)
                while (sourceResultSet.next()) {
                    // 值
                    StringBuilder values = new StringBuilder();
                    values.append("(");
                    for (int i = 1; i <= columnCount; i++) {
                        String key = metaData.getColumnLabel(i);//列名称
                        String val = sourceResultSet.getObject(key) == null ? null : sourceResultSet.getObject(key).toString();
                        if (val != null) {
                            if (val.contains("'")) {
                                val = val.replaceAll("'", "''");
                            }
                            val = "'" + val + "'";
                        }
                        values.append(val);
                        if (i < columnCount) {
                            values.append(",");
                        }
                    }
                    values.append(")");
                    vals.add(values.toString());
                    count++;
                    if (vals.size() >= buffer) {
                        saveOrUpdate(sqlInsertBegin.toString(), sqlInsertEnd, vals, targetConnection);
                        vals = new ArrayList<>();
                        log.debug("\033[40;32;0m 同步表：{} -> {} 进行中，当前已同步：{} \t\t\t\033[0m", config.getSourceTable(), config.getTargetTable(), count);
                    }
                }
                saveOrUpdate(sqlInsertBegin.toString(), sqlInsertEnd, vals, targetConnection);
                log.debug("\033[40;32;0m 同步表：{} -> {} 进行中，当前已同步：{} \t\t\t\033[0m", config.getSourceTable(), config.getTargetTable(), count);
            }
        }
    }

    /**
     * 更新或者插入 mysql 拼装语句
     *
     * @param sqlInsertBegin 更新头 insert into table (f1,f2,f3...) values
     * @param sqlInsertEnd   更新尾 on duplicate key update f1=values(f1),f2=values(f2),f3=values(f3)...
     * @param vals           值
     * @param connTarget     目标连接
     * @throws Exception 异常
     */
    private void saveOrUpdate(String sqlInsertBegin, String sqlInsertEnd, List<String> vals, Connection connTarget) throws Exception {
        if (!vals.isEmpty()) {
            StringBuilder sqlInsert = new StringBuilder();
            sqlInsert.append(sqlInsertBegin);
            for (int i = 0; i < vals.size(); i++) {
                sqlInsert.append(vals.get(i));
                if (i < vals.size() - 1) {
                    sqlInsert.append(",");
                }
            }
            sqlInsert.append(sqlInsertEnd);
            saveOrUpdate(connTarget, sqlInsert.toString());
        }
    }

    /**
     * 执行语句
     *
     * @param connTarget 目标连接
     * @param sqlInsert  语句
     * @throws Exception 异常
     */
    public void saveOrUpdate(Connection connTarget, String sqlInsert) throws Exception {
        try (PreparedStatement statementEval = connTarget.prepareStatement(sqlInsert)) {
            statementEval.execute();
        } catch (Exception e) {
            log.error("\033[40;32;0m 执行SQL失败！：\r\n{} \t\t\t\033[0m", sqlInsert);
            throw e;
        }
    }
}
