package io.github.mangocrisp.spring.taybct.tool.core.es.util;

import cn.hutool.core.collection.CollectionUtil;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import io.github.mangocrisp.spring.taybct.tool.core.es.dto.ESQuerySort;
import io.github.mangocrisp.spring.taybct.tool.core.es.dto.element.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * es 查询工具类
 *
 * @author xijieyin <br> 2023/1/11 14:22
 */
@Slf4j
public class ESQueryUtil {

    /**
     * 混合条件
     *
     * @param basic 基础的
     * @param extra 额外的
     * @return 混合后的条件
     */
    public AndOrNot mix(AndOrNot basic, AndOrNot extra) {
        Set<Exists> exists = Optional.ofNullable(basic.getExists()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getExists()).ifPresent(exists::addAll);
        if (!exists.isEmpty()) {
            basic.setExists(exists);
        }

        Set<Range> range = Optional.ofNullable(basic.getRange()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getRange()).ifPresent(range::addAll);
        if (!range.isEmpty()) {
            basic.setRange(range);
        }

        Set<Term> term = Optional.ofNullable(basic.getTerm()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getTerm()).ifPresent(term::addAll);
        if (!term.isEmpty()) {
            basic.setTerm(term);
        }

        Set<Wildcard> wildcard = Optional.ofNullable(basic.getWildcard()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getWildcard()).ifPresent(wildcard::addAll);
        if (!wildcard.isEmpty()) {
            basic.setWildcard(wildcard);
        }

        Set<Terms> terms = Optional.ofNullable(basic.getTerms()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getTerms()).ifPresent(terms::addAll);
        if (!terms.isEmpty()) {
            basic.setTerms(terms);
        }

        Set<Bool> bool = Optional.ofNullable(basic.getBool()).orElse(new LinkedHashSet<>());
        Optional.ofNullable(extra.getBool()).ifPresent(bool::addAll);
        if (!bool.isEmpty()) {
            basic.setBool(bool);
        }

        return basic;
    }

    /**
     * 生成 es 排序
     *
     * @param fieldSort 排序字段集合
     * @return es 排序
     */
    public static Sort genSort(LinkedHashSet<ESQuerySort> fieldSort) {
        return Sort.by(fieldSort.stream()
                .map(fs -> Optional.ofNullable(fs.getOrder())
                        .map(order -> {
                            if (order.equalsIgnoreCase(SortOrder.Asc.jsonValue())) {
                                return Sort.Order.asc(fs.getField());
                            } else if (order.equalsIgnoreCase(SortOrder.Desc.jsonValue())) {
                                return Sort.Order.desc(fs.getField());
                            }
                            return Sort.Order.by(fs.getField());
                        }).orElse(Sort.Order.by(fs.getField()))).collect(Collectors.toList()));
    }

    /**
     * 转换排序
     *
     * @param sortArray 这个可以是前端传过来的排序，大概可以是例如：
     *                  <br>{@code ["name asc","age","xxx desc"]}
     * @return 排序集合
     */
    public static LinkedHashSet<ESQuerySort> convertOrder(String[] sortArray) {
        LinkedHashSet<ESQuerySort> esQuerySorts = new LinkedHashSet<>();
        Arrays.stream(sortArray).forEach(s -> {
            String[] sa = s.split(" ");
            if (sa.length > 1) {
                esQuerySorts.add(new ESQuerySort(sa[0], sa[1].toLowerCase()));
            } else {
                esQuerySorts.add(new ESQuerySort(sa[0], "asc"));
            }
        });
        return esQuerySorts;
    }

    /**
     * 构建 es 查询用的 query
     *
     * @param bool    封装的查询
     * @param builder 查询 builder，这个是最终组成
     */
    public static BoolQuery.Builder buildQuery(Bool bool, BoolQuery.Builder builder) {
        // 这里是 and 查询
        Optional.ofNullable(bool.getMust()).ifPresent(must -> build(must, builder::must));
        // 这里是 and not 查询，里面包含所有的组成 not 的条件，例如 name != 'xxx' and age != 18
        Optional.ofNullable(bool.getMustNot()).ifPresent(mustNot -> build(mustNot, builder::mustNot));
        // 这里是 or 查询
        Optional.ofNullable(bool.getShould()).ifPresent(should -> build(should, builder::should));
        return builder;
    }

    /**
     * 构建
     *
     * @param andOrNot 获取构建对象的接口
     * @param b        构建器
     */
    public static void
    build(AndOrNot andOrNot, Function<Function<Query.Builder, ObjectBuilder<Query>>, BoolQuery.Builder> b) {
        // 这里是 is not null 查询
        Optional.ofNullable(andOrNot.getExists()).ifPresent(term ->
                term.forEach(tf -> b.apply(builder -> builder.exists(t -> t.field(tf.getField())))));
        // 这里是 = 查询
        Optional.ofNullable(andOrNot.getTerm()).ifPresent(term ->
                term.forEach(tf -> b.apply(builder -> builder.term(t -> t.field(tf.getField()).value(tf.getValue())))));
        // 这里是 in 查询
        Optional.ofNullable(andOrNot.getTerms()).ifPresent(terms ->
                terms.forEach(tsf -> b.apply(builder -> builder.terms(ts ->
                        ts.field(tsf.getField()).terms(filed -> filed.value(tsf.getValues().stream().map(FieldValue::of).toList()))))));
        // 这里是 like 查询 * 表示模糊
        Optional.ofNullable(andOrNot.getWildcard()).ifPresent(wildcard ->
                wildcard.forEach(wf -> b.apply(builder -> builder.wildcard(w -> w.field(wf.getField()).value(wf.getValue())))));
        // 这里是 区间 查询
        Optional.ofNullable(andOrNot.getRange()).ifPresent(range ->
                range.forEach(rf -> {
                    RangeQuery.Builder rangeBuilder = QueryBuilders.range();
                    // 你必须得有一个条件才能查询
                    final AtomicReference<Boolean> flat = new AtomicReference<>(false);
                    rangeBuilder.untyped(builder -> {
                        if (rf.getLt() != null) {
                            flat.set(true);
                            builder.lt(JsonData.of(rf.getLt()));
                        } else if (rf.getLte() != null) {
                            flat.set(true);
                            builder.lte(JsonData.of(rf.getLte()));
                        }
                        if (rf.getGt() != null) {
                            flat.set(true);
                            builder.gt(JsonData.of(rf.getGt()));
                        }
                        if (rf.getGte() != null) {
                            flat.set(true);
                            builder.gte(JsonData.of(rf.getGte()));
                        }
                        if (rf.getTimeZone() != null) {
                            builder.timeZone(rf.getTimeZone());
                        }
                        return builder;
                    });
                    if (flat.get()) {
                        b.apply(builder -> builder.range(rangeBuilder.build()));
                    }
                }));
        // 这里是新起一个 () 嵌套查询
        Optional.ofNullable(andOrNot.getBool()).ifPresent(boolInside ->
                boolInside.forEach(bf ->
                        b.apply(builder -> builder.bool(buildQuery(bf, co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders.bool()).build()))));

    }


    /**
     * CriteriaQuery 查询，这个可以看一下
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/5.0.0/reference/html/#elasticsearch.operations.criteriaquery">CriteriaQuery</a>
     *
     * @param elasticsearchOperations es 操作处理
     * @param criteriaSupplier        提供一个 Criteria 对象
     * @param number                  分页页码
     * @param size                    分页大小
     * @param fieldSort               排序字段
     * @param clazz                   类型
     * @param <E>                     泛型
     * @return 分页
     */
    public static <E> Page<E> criteriaQueryPage(ElasticsearchOperations elasticsearchOperations
            , Supplier<Criteria> criteriaSupplier
            , int number
            , int size
            , @NotNull LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        Pageable pageable = CollectionUtil.isEmpty(fieldSort) ? PageRequest.of(number, size) :
                PageRequest.of(number, size, genSort(fieldSort));
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteriaSupplier.get(), pageable);
        log.info("\r\n ES 查询语句： \r\n {} \r\n", criteriaQuery);
        SearchHits<E> searchHits = elasticsearchOperations.search(criteriaQuery, clazz);
        SearchPage<E> page = SearchHitSupport.searchPageFor(searchHits, criteriaQuery.getPageable());
        return (Page) SearchHitSupport.unwrapSearchHits(page);
    }

    /**
     * NativeSearchQuery 查询
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/5.0.0/reference/html/#elasticsearch.operations.nativesearchquery">NativeSearchQuery</a>
     *
     * @param elasticsearchOperations es 操作处理
     * @param object                  指定查询的类，这个类是用来与 es 存储对应的加了 {@linkplain  org.springframework.data.elasticsearch.annotations.Document @Document} 注解的实体类的类型
     * @param queryBuilderBiFunction  额外对 boolQuery 做拼接
     * @param number                  分页页码
     * @param size                    分页大小
     * @param fieldSort               排序字段
     * @param clazz                   类型
     * @param <E>                     泛型
     * @return 分页结果
     */
    public static <E> Page<E> nativeSearchQueryPage(ElasticsearchOperations elasticsearchOperations
            , E object
            , BiFunction<E, BoolQuery.Builder, BoolQuery.Builder> queryBuilderBiFunction
            , int number
            , int size
            , LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        return nativeSearchQueryPage(elasticsearchOperations
                , () -> queryBuilderBiFunction.apply(object, QueryBuilders.bool())
                , number
                , size
                , fieldSort
                , clazz);
    }

    /**
     * NativeSearchQuery 查询
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/5.0.0/reference/html/#elasticsearch.operations.nativesearchquery">NativeSearchQuery</a>
     *
     * @param elasticsearchOperations  es 操作处理
     * @param boolQueryBuilderSupplier 提供一个 boolQueryBuilder，可以是直接 {@code QueryBuilders.boolQuery()} 或者 {@link ESQueryUtil#buildQuery}
     * @param number                   分页页码
     * @param size                     分页大小
     * @param fieldSort                排序字段
     * @param clazz                    类型
     * @param <E>                      泛型
     * @return 分页结果
     */
    public static <E> Page<E> nativeSearchQueryPage(ElasticsearchOperations elasticsearchOperations
            , Supplier<BoolQuery.Builder> boolQueryBuilderSupplier
            , int number
            , int size
            , LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        NativeQueryBuilder nativeQueryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilderSupplier.get().build()));
        if (CollectionUtil.isNotEmpty(fieldSort)) {
            fieldSort.forEach(esQuerySort -> {
                if (esQuerySort.getField() != null) {
                    // 排序
                    if (SortOrder.Desc.jsonValue().equalsIgnoreCase(esQuerySort.getOrder())) {
                        nativeQueryBuilder.withSort(b -> b.field(f -> f.field(esQuerySort.getField()).order(SortOrder.Desc)));
                    } else {
                        nativeQueryBuilder.withSort(b -> b.field(f -> f.field(esQuerySort.getField()).order(SortOrder.Asc)));
                    }
                }
            });
        }
        nativeQueryBuilder.withPageable(PageRequest.of(number, size));
        NativeQuery searchQuery = nativeQueryBuilder.build();
        log.info("\r\n ES 查询语句： \r\n {} \r\n", nativeQueryBuilder.getQuery());
        // 设置查询真实总数
        searchQuery.setTrackTotalHits(true);
        SearchHits<E> searchHits = elasticsearchOperations.search(searchQuery, clazz);
        SearchPage<E> page = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
        return (Page) SearchHitSupport.unwrapSearchHits(page);
    }

    /**
     * 查询语句分页查询
     * <br> 官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/5.0.0/reference/html/#elasticsearch.operations.stringquery">StringQuery</a>
     *
     * @param elasticsearchOperations es 操作处理
     * @param source                  语句
     * @param number                  分页页码
     * @param size                    分页大小
     * @param fieldSort               排序字段
     * @param clazz                   类型
     * @param <E>                     泛型
     * @return 分页
     */
    public static <E> Page<E> stringQueryPage(ElasticsearchOperations elasticsearchOperations
            , String source
            , int number
            , int size
            , @NotNull LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        Pageable pageable = PageRequest.of(number, size);
        StringQuery stringQuery = CollectionUtil.isEmpty(fieldSort) ? new StringQuery(source, pageable) :
                new StringQuery(source
                        , pageable
                        , genSort(fieldSort));
        log.info("\r\n ES 查询语句： \r\n {} \r\n", stringQuery.getSource());
        // 设置查询真实总数
        stringQuery.setTrackTotalHits(true);
        SearchHits<E> searchHits = elasticsearchOperations.search(stringQuery, clazz);
        SearchPage<E> page = SearchHitSupport.searchPageFor(searchHits, stringQuery.getPageable());
        return (Page) SearchHitSupport.unwrapSearchHits(page);
    }

}
