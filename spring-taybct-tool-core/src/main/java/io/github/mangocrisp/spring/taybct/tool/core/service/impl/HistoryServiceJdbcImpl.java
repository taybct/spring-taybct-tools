package io.github.mangocrisp.spring.taybct.tool.core.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import io.github.mangocrisp.spring.taybct.tool.core.domain.HistoryEntity;
import io.github.mangocrisp.spring.taybct.tool.core.ds.DBHelper;
import io.github.mangocrisp.spring.taybct.tool.core.ds.JdbcTemplateUtil;
import io.github.mangocrisp.spring.taybct.tool.core.service.IHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;

import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 历史记录 Service 的 JDBC Template 实现
 *
 * @author XiJieYin <br> 2023/1/29 17:15
 */
@RequiredArgsConstructor
@Slf4j
public class HistoryServiceJdbcImpl implements IHistoryService {

    final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> getRecordByPrimaryKey(String dataSource
            , String tableName
            , String primaryKey
            , Serializable value
            , int pkTypes) {
        List<Map<String, Object>> maps = DBHelper.execSelectMap(JdbcTemplateUtil.getConnection(jdbcTemplate, dataSource)
                , String.format(QUERY_SQL, tableName, primaryKey)
                , new Object[]{value}
                , new int[]{pkTypes}
                , true);
        return maps.isEmpty() ? null : maps.get(0);
    }

    @Override
    @Async
    public void recordingHistory(String dataSource
            , String historyTableName
            , HistoryEntity historyEntity) {
        Connection connection = JdbcTemplateUtil.getConnection(jdbcTemplate, dataSource);
        // 默认使用 pgsql
        DbType datasourceDbType = DBHelper.getDbType(connection, DbType.POSTGRE_SQL);
        DBHelper.execUpdate(connection
                , String.format(SAVE_SQL, historyTableName)
                , ps -> {
                    // id
                    ps.setLong(1, historyEntity.getId());
                    // created_by
                    ps.setString(2, historyEntity.getCreateUser());
                    // created_time
                    ps.setTimestamp(3, new Timestamp(java.util.Date.from(historyEntity
                            .getCreateTime().atZone(ZoneId.systemDefault()).toInstant()).getTime()));
                    // 表名，数据来自哪个表
                    ps.setString(4, historyEntity.getTableName());
                    // 数据来源表的主键值
                    ps.setString(5, historyEntity.getPrimaryKey());
                    // 数据
                    String jsonData = historyEntity.getJsonData();
                    if (datasourceDbType.equals(DbType.ORACLE)) {
                        // 兼容 ORACLE 这里使用 CLOB 存储
                        StringReader reader = new StringReader(jsonData);
                        ps.setClob(6, reader, jsonData.length());
                    } else if (datasourceDbType.equals(DbType.POSTGRE_SQL)) {
                        // 兼容 PGSQL 使用 JSON 存储的时候，这里需要兼容类型
                        PGobject jsonObject = new PGobject();
                        jsonObject.setType("json");
                        jsonObject.setValue(jsonData);
                        ps.setObject(6, jsonObject);
                    } else {
                        // 其他数据库没有强要求的都使用字符串存储
                        ps.setString(6, jsonData);
                    }
                    // 操作类型
                    ps.setInt(7, historyEntity.getOperateType());
                    // 主键值
                    ps.setString(8, historyEntity.getPrimaryValue().toString());
                });
    }

}
