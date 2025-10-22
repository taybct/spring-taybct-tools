package io.github.taybct.tool.core.es.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import io.github.taybct.tool.core.es.condition.IESQueryConditions;
import io.github.taybct.tool.core.es.dto.ESQueryDTO;
import io.github.taybct.tool.core.es.dto.ESQuerySort;
import io.github.taybct.tool.core.es.dto.element.Bool;
import io.github.taybct.tool.core.es.service.IESService;
import io.github.taybct.tool.core.es.util.ESQueryUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashSet;
import java.util.function.BiFunction;

/**
 * ES 操作默认实现类
 *
 * @author xijieyin <br> 2022/8/4 19:02
 * @since 1.0.0
 */
@Slf4j
public class ESServiceImpl<T> implements IESService<T> {

    @Resource
    ElasticsearchOperations elasticsearchOperations;

    public IESQueryConditions<T> esQueryConditions() {
        return new IESQueryConditions<>() {
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return Class&lt;T&gt;
     * @author xijieyin <br> 2022/8/5 22:23
     * @since 1.0.0
     */
    public Class<T> getClazz() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * {@inheritDoc}
     *
     * @param queryDTO
     * @param queryBuilderBiFunction
     * @return Page&lt;T&gt;
     * @author xijieyin <br> 2022/8/5 22:23
     * @since 1.0.0
     */
    public Page<T> page(ESQueryDTO<? extends T> queryDTO, BiFunction<T, BoolQuery.Builder, BoolQuery.Builder> queryBuilderBiFunction) {
        return ESQueryUtil.nativeSearchQueryPage(elasticsearchOperations
                , queryDTO.getObject()
                , queryBuilderBiFunction
                , queryDTO.getNumber()
                , queryDTO.getSize()
                , queryDTO.getFieldSort()
                , getClazz());
    }

    /**
     * 直接传查询语句查询
     *
     * @param source    查询语句
     * @param number    分页页码
     * @param size      分页大小
     * @param fieldSort 排序字段
     * @return 分页结果
     */
    @Override
    public Page<T> page(String source, int number, int size, LinkedHashSet<ESQuerySort> fieldSort) {
        return ESQueryUtil.stringQueryPage(
                elasticsearchOperations
                , source
                , number
                , size
                , fieldSort
                , getClazz());
    }

    @Override
    public BoolQuery.Builder buildQuery(Bool bool, BoolQuery.Builder builder) {
        return ESQueryUtil.buildQuery(bool, builder);
    }

}
