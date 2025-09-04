package io.github.mangocrisp.spring.taybct.tool.core.service;

import io.github.mangocrisp.spring.taybct.tool.core.annotation.RecordHistory;
import io.github.mangocrisp.spring.taybct.tool.core.domain.DBOperateHistoryEntity;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;

import java.io.Serializable;
import java.util.Map;

/**
 * 数据库操作历史记录 Service，
 * <br>
 * 关于多数据源的设置，这个可以看一下注解 {@linkplain RecordHistory @RecordHistory}
 *
 * @author XiJieYin <br> 2023/1/29 16:38
 */
public interface IDBOperateHistoryService {

    /**
     * 查询语句
     */
    String QUERY_SQL = "select * from %s where %s = ?";
    /**
     * 保存语句，这里规定一下表结构一定是这几个字段就好了
     */
    String SAVE_SQL = "INSERT INTO %s (id,create_user,create_time,table_name,primary_key,json_data,operate_type,primary_value) VALUES(?,?,?,?,?,?,?,?)";

    /**
     * 根据主键获取数据记录，这里有考虑到，可能有多数据源的问题，所以这里可以指定是从哪个数据源去操作数据
     *
     * @param dataSource 数据源
     * @param tableName  表名
     * @param primaryKey 主键名
     * @param value      主键值
     * @param pkTypes    主键类型
     * @return 数据记录
     */
    default Map<String, Object> getRecordByPrimaryKey(String dataSource
            , String tableName
            , String primaryKey
            , Serializable value
            , int pkTypes) {
        throw new BaseException("没有找到历史记录 Service 的实现类");
    }


    /**
     * 记录历史表，这里有考虑到，可能有多数据源的问题，所以这里可以指定是从哪个数据源去操作数据
     *
     * @param dataSource             数据源
     * @param historyTableName       记录历史记录的表名
     * @param dbOperateHistoryEntity 历史记录
     */
    default void recordingHistory(String dataSource
            , String historyTableName
            , DBOperateHistoryEntity dbOperateHistoryEntity) {
        throw new BaseException("没有找到历史记录 Service 的实现类");
    }

}
