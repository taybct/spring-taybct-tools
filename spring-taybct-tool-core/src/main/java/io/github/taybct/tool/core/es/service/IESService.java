package io.github.taybct.tool.core.es.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import io.github.taybct.tool.core.es.condition.IESQueryConditions;
import io.github.taybct.tool.core.es.dto.ESQueryDTO;
import io.github.taybct.tool.core.es.dto.ESQuerySort;
import io.github.taybct.tool.core.es.dto.element.Bool;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ES 查询相关操作
 *
 * @author xijieyin <br> 2022/8/4 18:58
 * @since 1.0.0
 */
public interface IESService<T> {

    /**
     * 获取到操作的数据的类型
     *
     * @return {@code Class<T>}
     * @author xijieyin <br> 2022/8/4 19:05
     * @since 1.0.0
     */
    Class<T> getClazz();

    /**
     * 实体类查询条件构建
     *
     * @return {@code IESQueryConditions<T>}
     * @author xijieyin <br> 2022/8/4 18:58
     * @since 1.0.0
     */
    IESQueryConditions<T> esQueryConditions();

    /**
     * 分页查询
     *
     * @param queryDTO 查询请求数据传输对象
     * @return {@code Page<T>}
     * @author xijieyin <br> 2022/8/4 18:58
     * @since 1.0.0
     */
    default Page<T> page(ESQueryDTO<? extends T> queryDTO) {
        IESQueryConditions<T> iesQueryConditions = new IESQueryConditions<>() {
        };
        return page(queryDTO, iesQueryConditions::assembleBoolQuery);
    }


    /**
     * 分页查询，可以自定义查询条件
     *
     * @param queryDTO               查询请求数据传输对象
     * @param queryBuilderBiFunction bool 查询构建方法
     * @return {@code Page<T>}
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    Page<T> page(ESQueryDTO<? extends T> queryDTO, BiFunction<T, BoolQuery.Builder, BoolQuery.Builder> queryBuilderBiFunction);

    /**
     * 直接传查询语句查询
     *
     * @param source    查询语句
     * @param number    分页页码
     * @param size      分页大小
     * @param fieldSort 排序字段
     * @return 分页结果
     */
    Page<T> page(String source, int number, int size, @NotNull LinkedHashSet<ESQuerySort> fieldSort);

    /**
     * 构建 es 查询用的 query
     *
     * @param bool    封装的查询
     * @param builder 查询 builder，这个是最终组成
     */
    BoolQuery.Builder buildQuery(Bool bool, BoolQuery.Builder builder);

    /**
     * 构建自定义查询
     *
     * @param dto          查询条件传输对象
     * @param difference   区别不同的操作，相同，固定的条件定死在里面，difference 可以在外面定义不同的条件
     * @param queryBuilder 查询条件
     * @return NativeQueryBuilder
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    default BoolQuery.Builder getQueryBuilder(ESQueryDTO<T> dto
            , Consumer<BoolQuery.Builder> difference
            , BoolQuery.Builder queryBuilder) {
        // 使用查询实体类来查询
        esQueryConditions().assembleBoolQuery(dto.getObject(), queryBuilder);
        customizeQueryCondition(dto, queryBuilder);
        difference.accept(queryBuilder);
        // 这里重装条件
        Optional.ofNullable(dto.getBool()).ifPresent(boolInside ->
                buildQuery(boolInside, queryBuilder));
        return queryBuilder;
    }

    /**
     * 自定义查询条件<br>
     * 主要继承这个，在这个里面写自己定义的查询条件
     *
     * @param dto          查询条件传输对象
     * @param queryBuilder 布尔查询
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    default void customizeQueryCondition(ESQueryDTO<T> dto, BoolQuery.Builder queryBuilder) {
    }


    /**
     * 多条件查询分页
     *
     * @param dto        查询条件数据传输对象
     * @param difference 区别不同的操作，相同，固定的条件定死在里面，difference 可以在外面定义不同的条件
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    default Page<T> termsQueryPage(ESQueryDTO<T> dto, Consumer<BoolQuery.Builder> difference) {
        return page(dto, (entity, queryBuilder) -> getQueryBuilder(dto, difference, queryBuilder));
    }

    /**
     * 多条件查询可以指定是否分页
     *
     * @param dto        查询条件数据传输对象
     * @param difference 区别不同的操作，相同，固定的条件定死在里面，difference 可以在外面定义不同的条件
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    default List<T> search(ESQueryDTO<T> dto
            , Consumer<BoolQuery.Builder> difference) {
        return search(dto, difference, pageData -> {
        });
    }

    /**
     * 多条件查询可以指定是否分页
     *
     * @param dto        查询条件数据传输对象
     * @param difference 区别不同的操作，相同，固定的条件定死在里面，difference 可以在外面定义不同的条件
     * @param pageData   返回 list 同时也可以提供分页数据
     * @author xijieyin <br> 2022/8/4 18:59
     * @since 1.0.0
     */
    default List<T> search(ESQueryDTO<T> dto
            , Consumer<BoolQuery.Builder> difference
            , Consumer<Page<T>> pageData) {
        // 查询分页
        Page<T> page = termsQueryPage(dto, difference);
        // 把分页信息抛出
        pageData.accept(page);
        // 返回分页列表
        return page.get().collect(Collectors.toList());
    }
}
