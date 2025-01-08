package io.github.mangocrisp.spring.taybct.tool.core.bean;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 转换类型接口
 *
 * @author xijieyin <br> 2022/8/5 17:38
 * @since 1.0.0
 */
public interface ITypeConvert {

    /**
     * 转换实体类集合为输出对象集合
     *
     * @param entityCollection 集合
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code List<O>}
     * @author xijieyin <br> 2022/8/5 17:38
     * @since 1.0.0
     */
    default <T, O extends T> List<O> entityCollectionConvert2SomeCollection(List<T> entityCollection, Class<O> clazz, String... ignoreProperties) {
        return (entityCollection.isEmpty() ? new ArrayList(0) :
                BeanUtil.copyToList(entityCollection, clazz, CopyOptions.create().setIgnoreProperties(ignoreProperties)));
    }

    /**
     * 输入集合转实体对象集合
     *
     * @param inCollection     集合
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code List<T>}
     * @author xijieyin <br> 2022/8/5 17:38
     * @since 1.0.0
     */
    default <T, I extends T> List<T> someCollectionConvert2EntityCollection(List<I> inCollection, Class<T> clazz, String... ignoreProperties) {
        return (inCollection.isEmpty() ? new ArrayList(0) :
                BeanUtil.copyToList(inCollection, clazz, CopyOptions.create().setIgnoreProperties(ignoreProperties)));
    }

    /**
     * 转换实体类对象为输出对象
     *
     * @param entity           对象
     * @param clazz            对象类型
     * @param ignoreProperties 忽略字段
     * @return O
     * @author xijieyin <br> 2022/8/5 17:39
     * @since 1.0.0
     */
    default <T, O extends T> O entityConvert2Some(T entity, Class<O> clazz, String... ignoreProperties) {
        return (ObjectUtil.isEmpty(entity) ? null : BeanUtil.copyProperties(entity, clazz, ignoreProperties));
    }

    /**
     * 输入对象转实体类
     *
     * @param in               输入对象
     * @param clazz            对象类型
     * @param ignoreProperties 忽略的字段
     * @return T
     * @author xijieyin <br> 2022/8/5 17:39
     * @since 1.0.0
     */
    default <T, I extends T> T someConvert2Entity(I in, Class<T> clazz, String... ignoreProperties) {
        return (ObjectUtil.isEmpty(in) ? null : BeanUtil.copyProperties(in, clazz, ignoreProperties));
    }

    /**
     * 转换分页对象
     *
     * @param entityPage       返回的分页对象
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code IPage<O>}
     * @author xijieyin <br> 2022/8/5 17:39
     * @since 1.0.0
     */
    default <T, O extends T> IPage<O> entityPageConvert2OutPage(IPage<T> entityPage, Class<O> clazz, String... ignoreProperties) {
        if (ObjectUtil.isNotEmpty(entityPage)) {
            entityPage.setRecords((List<T>) entityCollectionConvert2SomeCollection(entityPage.getRecords(), clazz, ignoreProperties));
            return (IPage<O>) entityPage;
        }
        return new Page<>();
    }

    /**
     * 转换成输出结果
     *
     * @param r                返回的结果
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code R<O>}
     * @author xijieyin <br> 2022/8/5 17:39
     * @since 1.0.0
     */
    default <T, O extends T> R<O> convert2SomeResult(R<T> r, Class<O> clazz, String... ignoreProperties) {
        return (!r.isOk() ? ((R<O>) r) : R.data(entityConvert2Some(r.getData(), clazz, ignoreProperties)));
    }

    /**
     * 转换成输出结果集合
     *
     * @param r                返回的结果
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code R<List < O>>}
     * @author xijieyin <br> 2022/8/5 17:40
     * @since 1.0.0
     */
    default <T, O extends T> R<List<O>> convert2SomeResultCollection(R<List<T>> r, Class<O> clazz, String... ignoreProperties) {
        return (!r.isOk() ? ((R) r) : R.data(entityCollectionConvert2SomeCollection(r.getData(), clazz, ignoreProperties)));
    }

    /**
     * 转换成输出结果分页
     *
     * @param r                返回的结果
     * @param clazz            需要转换的类型
     * @param ignoreProperties 忽略的字段
     * @return {@code R<IPage < O>>}
     * @author xijieyin <br> 2022/8/5 17:40
     * @since 1.0.0
     */
    default <T, O extends T> R<IPage<O>> convert2SomeResultPage(R<IPage<T>> r, Class<O> clazz, String... ignoreProperties) {
        return (!r.isOk() ? ((R) r) : R.data(entityPageConvert2OutPage(r.getData(), clazz, ignoreProperties)));
    }
}
