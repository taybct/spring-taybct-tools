package io.github.mangocrisp.spring.taybct.tool.core.ds.sync.handler;

import io.github.mangocrisp.spring.taybct.tool.core.ds.DBHelper;
import io.github.mangocrisp.spring.taybct.tool.core.ds.sync.DataSyncConfig;
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
 * oracle 数据同步处理类
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/4/15 10:17
 */
@Slf4j
public class DataSyncOracleHandler implements IDataSyncHandler {

    /**
     * 单次同步数据量，默认为 512
     */
    private static final int buffer = 512;

    @SneakyThrows
    @Override
    public PreparedStatement queryStatement(Connection conn, DataSyncConfig config) {
        if (StringUtil.isNotEmpty(config.getSourceSchema())) {
            // 如果有手动指定 schema
            conn.setSchema(config.getSourceSchema());
        }
        return conn.prepareStatement(config.getSqlSelect());
    }

    @SneakyThrows
    @Override
    public void sync(DataSyncConfig config
            , BiFunction<Connection, DataSyncConfig, PreparedStatement> queryStatement) {
        try (Connection sourceConnection = DBHelper.conn(config.getSourceDriver(), config.getSourceUrl(), config.getSourceUser(), config.getSourcePass());
             Connection targetConnection = DBHelper.conn(config.getTargetDriver(), config.getTargetUrl(), config.getTargetUser(), config.getTargetPass());
             PreparedStatement sourceStatement = queryStatement.apply(sourceConnection, config)) {

            //先查询一下目标表的主键
            String sqlGetPrimaryKey = """
                    SELECT a.COLUMN_NAME 
                     FROM user_cons_columns a,user_constraints b 
                     WHERE a.constraint_name = b.constraint_name 
                     AND b.constraint_type = 'P' 
                     AND a.table_name = ?""";
            PreparedStatement targetStatement = targetConnection.prepareStatement(sqlGetPrimaryKey);
            targetStatement.setString(1, config.getTargetTable());
            ResultSet targetResultSet = targetStatement.executeQuery();
            //可能有多个主键
            List<String> targetPrimaryKeys = new ArrayList<>();
            //主键
            List<String> pks = new ArrayList<>();
            while (targetResultSet.next()) {
                String col = targetResultSet.getString("COLUMN_NAME");
                targetPrimaryKeys.add(String.format("T.%s=S.%s", col, col));
                //得到 [{T.ID=S.ID},{T.ID1=S.ID2},{T.ID3=S.ID3}]这样的数组
                pks.add(col.toUpperCase());
            }
            targetResultSet.close();
            targetStatement.close();


            String lastSyncTime = "1970-01-01 00:00:00";
            targetStatement = targetConnection.prepareStatement(config.getSqlLastSyncTime());
            targetResultSet = targetStatement.executeQuery();
            while (targetResultSet.next()) {
                Object date = targetResultSet.getDate(config.getFieldLastSyncTime());
                Object time = targetResultSet.getTime(config.getFieldLastSyncTime());
                if (date!=null && time != null) {
                    lastSyncTime = date + " " + time;
                    log.debug("\033[40;32;0m 上一次更新时间：{} \t\t\t\033[0m",lastSyncTime);
                    break;
                }
            }
            targetResultSet.close();
            targetStatement.close();

            sourceStatement.setString(1, lastSyncTime);
            try (ResultSet sourceResultSet = sourceStatement.executeQuery()) {

                ResultSetMetaData metaData = sourceResultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                //拿到所有的列
                //List<String> cols = new ArrayList<>();
                //更新
                List<String> updateTarget = new ArrayList<>();
                //插入
                List<String> insertTarget = new ArrayList<>();
                //值
                List<String> valueTarget = new ArrayList<>();

                for (int i = 1; i <= columnCount; i++) {
                    String col = metaData.getColumnLabel(i).toUpperCase();//列名称
                    //cols.add(col);
                    if (!pks.contains(col)) {
                        updateTarget.add(String.format("T.%s=S.%s", col, col));
                    }
                    insertTarget.add(String.format("T.%s", col));
                    valueTarget.add(String.format("S.%s", col));
                }

                //主键
                String primaryKey = StringUtil.join(targetPrimaryKeys, " AND ");
                //更新
                String update = StringUtil.join(updateTarget, " , ");
                //插入
                String insert = StringUtil.join(insertTarget, " , ");
                //值
                String value = StringUtil.join(valueTarget, " , ");


                //using 里面的查询语句
                List<String> sourceUsing = new ArrayList<>();
                //统计同步数量
                int count = 0;
                // 4.处理数据库的返回结果(使用ResultSet类)
                while (sourceResultSet.next()) {
                    StringBuilder select = new StringBuilder("select ");
                    //键值对
                    List<String> kvs = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String key = metaData.getColumnLabel(i);//列名称
                        String val = null;
                        String columnType = metaData.getColumnTypeName(i);//列类型
                        if (columnType.equalsIgnoreCase("DATETIME") || columnType.equalsIgnoreCase("DATE")) {
                            if (sourceResultSet.getString(key) != null && !sourceResultSet.getString(key).isEmpty()) {
                                val = sourceResultSet.getString(key);
                                if (StringUtil.isNotEmpty(val)) {
                                    val = String.format("TO_DATE('%s','yyyy-MM-dd HH24:mi:ss')", val);
                                }
                            }
                        } else {
                            if (sourceResultSet.getString(key) != null && !sourceResultSet.getString(key).isEmpty()) {
                                val = sourceResultSet.getString(key);
                                //如果是有单引号的要把所有的单引号替换成两个单引号，然后括号括起来的就是'''desc'''这样的，插入的时候''会再变成'
                                if (val.contains("'")) {
                                    val = val.replaceAll("'", "''");
                                }
                                val = String.format("'%s'", val);
                            }
                        }
                        if (StringUtil.isNotEmpty(val)) {
                            val = String.format("%s AS %s", val, key);
                        } else {
                            val = String.format("NULL AS %s", key);
                        }
                        kvs.add(val);
                    }
                    select.append(StringUtil.join(kvs, ","));
                    select.append(" FROM DUAL");
                    //到这里：select '' AS A from dual

                    sourceUsing.add(select.toString());
                    count++;
                    if (sourceUsing.size() >= buffer) {
                        saveOrUpdate(targetConnection, config.getTargetTable(), sourceUsing, primaryKey, update, insert, value);
                        sourceUsing = new ArrayList<>();
                        log.debug("\033[40;32;0m 同步表：{} -> {} 进行中，当前已同步：{} \t\t\t\033[0m", config.getSourceTable(), config.getTargetTable(), count);
                    }
                }
                saveOrUpdate(targetConnection, config.getTargetTable(), sourceUsing, primaryKey, update, insert, value);
                log.debug("\033[40;32;0m 同步表：{} -> {} 进行中，当前已同步：{} \t\t\t\033[0m", config.getSourceTable(), config.getTargetTable(), count);
            }
        }
    }

    /**
     * 插入或者更新 oracle
     *
     * @param connTarget  连接目标
     * @param targetTable 目标表
     * @param sourceUsing 源表查询的临时表 数据是 select语句的数组 最后拼成 select 1 id from dual UNION all select .... from dual 这样的
     * @param primaryKey  主键可以有多个主键 T.ID=S.ID AND T.ID2=S.ID2
     * @param update      更新语句 T.A=S.A,T.B=S.B
     * @param insert      插入语句 T.A,T.B
     * @param value       插入值 S.A,S.B
     * @throws Exception 异常
     */
    public void saveOrUpdate(Connection connTarget, String targetTable, List<String> sourceUsing, String primaryKey, String update, String insert, String value) throws Exception {
        //oracle merge into的模板 MERGE INTO {源表} T USING({查询}) S ON ({主键}) WHEN MATCHED THEN UPDATE SET {更新} WHEN NOT MATCHED THEN ({插入}) VALUES ({值})
        if (!sourceUsing.isEmpty()) {
            String mergeTemplate = "MERGE INTO %s T USING(%s) S ON (%s) WHEN MATCHED THEN UPDATE SET %s WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)";
            String using = StringUtil.join(sourceUsing, " UNION ALL ");
            //拼成语句
            String sqlInsert = String.format(mergeTemplate, targetTable, using, primaryKey, update, insert, value);
            saveOrUpdate(connTarget, sqlInsert);
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
