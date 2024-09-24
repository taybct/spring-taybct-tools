package io.github.mangocrisp.spring.taybct.tool.core.bean.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangocrisp.spring.taybct.tool.core.bean.BaseEntity;
import io.github.mangocrisp.spring.taybct.tool.core.request.SqlQueryParams;
import io.github.mangocrisp.spring.taybct.tool.core.util.MyBatisUtil;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基础 service 继承自 mybatis plus 的 Iservice
 *
 * @author xijieyin <br> 2022/8/26 16:13
 * @since 1.0.2
 */
public interface IBaseService<T extends BaseEntity> extends IService<T> {


    /**
     * 自定义查询条件，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的条件,
     * 这个查询条件是适用于分页和不分页的，就是说，分页不分页都是使用这个查询条件来查询，
     * 除非，你自己继承之后再做判断
     * 你可以继承他来修改你自己定义的查询条件
     *
     * @param dto        查询参数
     * @param pageParams 分页参数
     * @return Wrapper&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:25
     * @since 1.0.2
     */
    default Wrapper<T> customizeQueryWrapper(T dto, SqlQueryParams pageParams) {
        return MyBatisUtil.genQueryWrapper(dto, pageParams);
    }

    /**
     * 自定义查询条件，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的条件,
     * 这个查询条件是适用于分页和不分页的，就是说，分页不分页都是使用这个查询条件来查询，
     * 除非，你自己继承之后再做判断
     * 你可以继承他来修改你自己定义的查询条件
     *
     * @param params 所有参数
     * @return Wrapper&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:25
     * @since 1.0.2
     */
    default Wrapper<T> customizeQueryWrapper(Map<String, Object> params) {
        return MyBatisUtil.genQueryWrapper(params, getEntityClass());
    }

    /**
     * 自定义查询分页，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的分页
     * 你可以继承他来修改你自己定义的分页
     *
     * @param pageParams 分页参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:38
     * @since 1.0.2
     */
    default IPage<T> customizeQueryPage(SqlQueryParams pageParams) {
        return MyBatisUtil.genPage(pageParams);
    }

    /**
     * 自定义查询分页，这里默认是使用 {@link MyBatisUtil} 根据查询参数生成的分页
     * 你可以继承他来修改你自己定义的分页
     *
     * @param params 所有参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:38
     * @since 1.0.2
     */
    default IPage<T> customizeQueryPage(Map<String, Object> params) {
        return MyBatisUtil.genPage(params);
    }

    /**
     * 自定义列表查询
     *
     * @param dto        查询参数
     * @param pageParams SQL 查询参数
     * @return List&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:39
     * @since 1.0.2
     */
    default List<? extends T> customizeList(T dto, SqlQueryParams pageParams) {
        Page<T> page = (Page<T>) customizeQueryPage(pageParams);
        // 这里不查询页数
        page.setSearchCount(false);
        return Optional.ofNullable(page(page, customizeQueryWrapper(dto, pageParams)))
                .map(IPage::getRecords)
                .map(list -> {
                    mergeQueryExpansion(list);
                    return list;
                }).orElseGet(Collections::emptyList);
    }

    /**
     * 自定义列表查询
     *
     * @param params 所有参数
     * @return List&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:39
     * @since 1.0.2
     */
    default List<? extends T> customizeList(Map<String, Object> params) {
        Page<T> page = (Page<T>) customizeQueryPage(params);
        // 这里不查询页数
        page.setSearchCount(false);
        return Optional.ofNullable(page(page, customizeQueryWrapper(params)))
                .map(IPage::getRecords)
                .map(list -> {
                    mergeQueryExpansion(list);
                    return list;
                }).orElseGet(Collections::emptyList);
    }

    /**
     * 自定义分页查询
     *
     * @param dto        查询参数
     * @param pageParams 分页参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:39
     * @since 1.0.2
     */
    default IPage<? extends T> customizePage(T dto, SqlQueryParams pageParams) {
        return Optional.ofNullable(page(customizeQueryPage(pageParams), customizeQueryWrapper(dto, pageParams)))
                .map(page -> {
                    mergeQueryExpansion(page.getRecords());
                    return page;
                }).orElse(null);
    }

    /**
     * 自定义分页查询
     *
     * @param params 所有参数
     * @return IPage&lt;T&gt;
     * @author xijieyin <br> 2022/8/26 16:39
     * @since 1.0.2
     */
    default IPage<? extends T> customizePage(Map<String, Object> params) {
        return Optional.ofNullable(page(customizeQueryPage(params), customizeQueryWrapper(params)))
                .map(page -> {
                    mergeQueryExpansion(page.getRecords());
                    return page;
                }).orElse(null);
    }

    /**
     * 自定义保存
     *
     * @param entity 保存的实体
     * @author xijieyin <br> 2022/9/26 11:30
     * @since 1.0.4
     */
    default boolean customizeSave(T entity) {
        return save(entity) && saveExpansion(Collections.singletonList(entity));
    }

    /**
     * 自定义批量保存
     *
     * @param entityList 实体类集合
     * @author xijieyin <br> 2022/9/26 11:36
     * @since 1.0.4
     */
    default boolean customizeSaveBatch(Collection<? extends T> entityList) {
        return saveBatch((Collection<T>) entityList) && saveExpansion(entityList);
    }

    /**
     * 自定义通过 id 查询信息
     *
     * @param id 主键 id
     * @author xijieyin <br> 2022/9/26 11:38
     * @since 1.0.4
     */
    default T customizeGetById(Serializable id) {
        return Optional.ofNullable(getById(id))
                .map(t -> {
                    mergeQueryExpansion(Collections.singletonList(t));
                    return t;
                }).orElse(null);
    }

    /**
     * 自定义根据 id 修改信息
     *
     * @param entity 实体类
     * @author xijieyin <br> 2022/9/26 11:41
     * @since 1.0.4
     */
    default boolean customizeUpdateById(T entity) {
        // 更新的时候不允许修改创建时间和创建用户
        entity.setCreateTime(null);
        entity.setCreateUser(null);
        return updateById(entity) && saveExpansion(Collections.singletonList(entity));
    }

    /**
     * 自定义根据 id 批量修改信息
     *
     * @return boolean
     * @author xijieyin <br> 2022/9/26 11:42
     * @since 1.0.4
     */
    default boolean customizeUpdateBatchById(Collection<? extends T> entityList) {
        // 更新的时候不允许修改创建时间和创建用户
        entityList.forEach(entity -> {
            entity.setCreateTime(null);
            entity.setCreateUser(null);
        });
        return updateBatchById((Collection<T>) entityList) && saveExpansion(entityList);
    }

    /**
     * 自定义根据 id 删除
     *
     * @return boolean
     * @author xijieyin <br> 2022/9/26 11:44
     * @since 1.0.4
     */
    default boolean customizeRemoveById(Serializable id) {
        return removeById(id) && removeExpansion(Collections.singletonList(id));
    }

    /**
     * 自定义根据 id 批量删除
     *
     * @return boolean
     * @author xijieyin <br> 2022/9/26 11:45
     * @since 1.0.4
     */
    default boolean customizeRemoveByIds(Collection<?> list) {
        return removeByIds(list) && removeExpansion(list);
    }

    /**
     * 合并查询的扩展字段
     *
     * @param list 查询结果
     * @author xijieyin <br> 2022/9/26 11:27
     * @since 1.0.4
     */
    default void mergeQueryExpansion(List<T> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            Optional.ofNullable(
                    queryExpansion(list.stream()
                            .map(T::getId).collect(Collectors.toList()))
            ).ifPresent(e -> list.forEach(t -> t.setExpansion(e.get(t.getId()))));
        }
    }

    /**
     * 根据 id 查询表扩展字段
     *
     * @param ids 查询结果的 id
     * @return {@code Map<Object> 结构是 id:{a:b,c:d} }
     * @author xijieyin <br> 2022/9/26 10:52
     * @since 1.0.4
     */
    default Map<Serializable, Map<String, Object>> queryExpansion(List<Serializable> ids) {
        return Collections.emptyMap();
    }

    /**
     * 更新扩展字段，没有新增，只有修改，因为是在原实体的基础上加扩展字段属于是
     *
     * @param list 更新成功后的实体（可能有多个，即批量操作的）
     * @return boolean 是否操作成功
     * @author xijieyin <br> 2022/9/26 10:54
     * @since 1.0.4
     */
    default boolean saveExpansion(Collection<? extends T> list) {
        return true;
    }

    /**
     * 删除扩展
     *
     * @param list 需要删除的实体的主键
     * @return boolean 是否操作成功
     * @author xijieyin <br> 2022/9/26 11:07
     * @since 1.0.4
     */
    default boolean removeExpansion(Collection<?> list) {
        return true;
    }

}
