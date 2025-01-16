package io.github.mangocrisp.spring.taybct.tool.core.handle;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.MybatisOptional;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;

import java.io.Serializable;
import java.util.Collection;

/**
 * <pre>
 * 将数据库的单表的简单（增删改查）操作同步到任何处
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/13 14:52
 */
public interface SyncToAnywhereHandler<T, PK extends Serializable> {

    /**
     * 未知操作
     *
     * @param dtoCollection 数据
     * @return 结果
     */
    default Object unknown(Collection<T> dtoCollection) {
        throw new RuntimeException("unknown");
    }

    /**
     * 新增
     *
     * @param dtoCollection 新增对象
     * @return 新增结果
     */
    default boolean insert(Collection<T> dtoCollection) {
        return false;
    }

    /**
     * 修改
     *
     * @param dtoCollection 修改对象
     * @return 修改结果
     */
    default boolean update(Collection<T> dtoCollection) {
        return false;
    }

    /**
     * 删除
     *
     * @param dtoCollection 删除集合
     * @param convert       要删除的对象类型
     * @return 删除结果
     */
    default boolean delete(Collection<PK> dtoCollection, Class<T> convert) {
        return false;
    }

    /**
     * 分页查询
     *
     * @param params                查询参数
     * @param queryConditionHandler 查询条件处理器
     * @param queryDataClass        查询对象类型
     * @return 分页结果
     */
    default IPage<T> page(JSONObject params, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        return page(MyBatisUtil
                .<T>mybatisOptional()
                .params(params), queryConditionHandler, queryDataClass);
    }

    /**
     * 分页查询
     *
     * @param params                查询参数
     * @param sqlPageParams         分页参数
     * @param queryConditionHandler 查询条件处理器
     * @param queryDataClass        查询对象类型
     * @return 分页结果
     */
    default IPage<T> page(JSONObject params, SqlPageParams sqlPageParams, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        return page(MyBatisUtil
                .<T>mybatisOptional()
                .params(params)
                .page(sqlPageParams), queryConditionHandler, queryDataClass);
    }

    /**
     * 分页查询
     *
     * @param mybatisOptional       查询参数
     * @param queryConditionHandler 查询条件处理器
     * @param queryDataClass        查询对象类型
     * @return 分页结果
     */
    default IPage<T> page(MybatisOptional<T> mybatisOptional, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        return mybatisOptional.getPage().genPage();
    }

    /**
     * 分页查询
     *
     * @param params                查询参数
     * @param queryConditionHandler 查询条件处理器
     * @param queryDataClass        查询对象类型
     * @return 分页结果
     */
    default long total(JSONObject params, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        return total(MyBatisUtil
                .<T>mybatisOptional()
                .params(params), queryConditionHandler, queryDataClass);
    }

    /**
     * 分页查询
     *
     * @param mybatisOptional       查询参数
     * @param queryConditionHandler 查询条件处理器
     * @param queryDataClass        查询对象类型
     * @return 分页结果
     */
    default long total(MybatisOptional<T> mybatisOptional, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        return 0;
    }

}
