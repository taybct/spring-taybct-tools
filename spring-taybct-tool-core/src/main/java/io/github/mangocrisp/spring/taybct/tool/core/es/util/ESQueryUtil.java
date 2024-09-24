package io.github.mangocrisp.spring.taybct.tool.core.es.util;

import cn.hutool.core.collection.CollectionUtil;
import io.github.mangocrisp.spring.taybct.tool.core.es.dto.ESQuerySort;
import io.github.mangocrisp.spring.taybct.tool.core.es.dto.element.AndOrNot;
import io.github.mangocrisp.spring.taybct.tool.core.es.dto.element.Bool;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
     * 生成 es 排序
     *
     * @param fieldSort 排序字段集合
     * @return es 排序
     */
    public static Sort genSort(LinkedHashSet<ESQuerySort> fieldSort) {
        return Sort.by(fieldSort.stream()
                .map(fs -> Optional.ofNullable(fs.getOrder())
                        .map(order -> {
                            if (order.equalsIgnoreCase(SortOrder.ASC.toString())) {
                                return Sort.Order.asc(fs.getField());
                            } else if (order.equalsIgnoreCase(SortOrder.DESC.toString())) {
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
     * 生成 ES 查询的语句
     *
     * @param searchQuery ES 的 Query 对象
     * @return String
     * @author xijieyin <br> 2022/8/4 19:07
     * @since 1.0.0
     */
    public static String genSearch(NativeSearchQuery searchQuery) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(searchQuery.getQuery());
        return searchSourceBuilder.toString();
    }

    /**
     * 构建 es 查询用的 query
     *
     * @param bool    封装的查询
     * @param builder 查询 builder，这个是最终组成
     */
    public static BoolQueryBuilder buildQuery(Bool bool, BoolQueryBuilder builder) {
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
     * @param andOrNot       获取构建对象的接口
     * @param handleConsumer 如何处理生成的 builder
     */
    public static void build(AndOrNot andOrNot, Consumer<QueryBuilder> handleConsumer) {
        // 这里是 is not null 查询
        Optional.ofNullable(andOrNot.getExists()).ifPresent(term ->
                term.forEach(t -> handleConsumer.accept(QueryBuilders.existsQuery(t.getField()))));
        // 这里是 = 查询
        Optional.ofNullable(andOrNot.getTerm()).ifPresent(term ->
                term.forEach(t -> handleConsumer.accept(QueryBuilders.termQuery(t.getField(), t.getValue()))));
        // 这里是 in 查询
        Optional.ofNullable(andOrNot.getTerms()).ifPresent(terms ->
                terms.forEach(ts -> handleConsumer.accept(QueryBuilders.termsQuery(ts.getField(), ts.getValues()))));
        // 这里是 like 查询 * 表示模糊
        Optional.ofNullable(andOrNot.getWildcard()).ifPresent(wildcard ->
                wildcard.forEach(w -> handleConsumer.accept(QueryBuilders.wildcardQuery(w.getField(), w.getValue()))));
        // 这里是 区间 查询
        Optional.ofNullable(andOrNot.getRange()).ifPresent(range -> range.forEach(r -> {
            RangeQueryBuilder queryBuilder = QueryBuilders.rangeQuery(r.getField());
            // 你必须得有一个条件才能查询
            boolean flat = false;
            if (r.getLt() != null) {
                flat = true;
                queryBuilder.lt(r.getLt());
            } else if (r.getLte() != null) {
                flat = true;
                queryBuilder.lte(r.getLte());
            }
            if (r.getGt() != null) {
                flat = true;
                queryBuilder.gt(r.getGt());
            }
            if (r.getGte() != null) {
                flat = true;
                queryBuilder.gte(r.getGte());
            }
            if (r.getTimeZone() != null) {
                queryBuilder.timeZone(r.getTimeZone());
            }
            if (flat) {
                handleConsumer.accept(queryBuilder);
            }
        }));
        // 这里是新起一个 () 嵌套查询
        Optional.ofNullable(andOrNot.getBool()).ifPresent(boolInside ->
                boolInside.forEach(b -> handleConsumer.accept(buildQuery(b, QueryBuilders.boolQuery()))));
    }


    /**
     * CriteriaQuery 查询，这个可以看一下
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/4.3.10/reference/html/#elasticsearch.operations.criteriaquery">CriteriaQuery</a>
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
        log.debug("\r\n ES 查询语句： \r\n {} \r\n", criteriaQuery);
        SearchHits<E> searchHits = elasticsearchOperations.search(criteriaQuery, clazz);
        SearchPage<E> page = SearchHitSupport.searchPageFor(searchHits, criteriaQuery.getPageable());
        return (Page) SearchHitSupport.unwrapSearchHits(page);
    }

    /**
     * NativeSearchQuery 查询
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/4.3.10/reference/html/#elasticsearch.operations.nativesearchquery">NativeSearchQuery</a>
     *
     * @param elasticsearchOperations    es 操作处理
     * @param object                     指定查询的类，这个类是用来与 es 存储对应的加了 {@linkplain  org.springframework.data.elasticsearch.annotations.Document @Document} 注解的实体类的类型
     * @param boolQueryBuilderBiFunction 额外对 boolQuery 做拼接
     * @param number                     分页页码
     * @param size                       分页大小
     * @param fieldSort                  排序字段
     * @param clazz                      类型
     * @param <E>                        泛型
     * @return 分页结果
     */
    public static <E> Page<E> nativeSearchQueryPage(ElasticsearchOperations elasticsearchOperations
            , E object
            , BiFunction<E, BoolQueryBuilder, BoolQueryBuilder> boolQueryBuilderBiFunction
            , int number
            , int size
            , LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        return nativeSearchQueryPage(elasticsearchOperations
                , () -> boolQueryBuilderBiFunction.apply(object, QueryBuilders.boolQuery())
                , number
                , size
                , fieldSort
                , clazz);
    }

    /**
     * NativeSearchQuery 查询
     * <br>官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/4.3.10/reference/html/#elasticsearch.operations.nativesearchquery">NativeSearchQuery</a>
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
            , Supplier<BoolQueryBuilder> boolQueryBuilderSupplier
            , int number
            , int size
            , @NotNull LinkedHashSet<ESQuerySort> fieldSort
            , Class<E> clazz) {
        // 组装查询条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilderSupplier.get());
        if (CollectionUtil.isNotEmpty(fieldSort)) {
            fieldSort.forEach(esQuerySort -> {
                if (esQuerySort.getField() != null) {
                    // 排序
                    // 排序条件的顺序是很重要的。结果首先按第一个条件排序，仅当结果集的第一个 sort 值完全相同时才会按照第二个条件进行排序，以此类推。
                    FieldSortBuilder sortBuilder = SortBuilders.fieldSort(esQuerySort.getField());
                    // 默认按顺序排
                    sortBuilder.order(SortOrder.ASC);
                    if (esQuerySort.getOrder().equalsIgnoreCase(SortOrder.DESC.toString())) {
                        sortBuilder.order(SortOrder.DESC);
                    }
                    sortBuilder.sortMode(SortMode.MIN);
                    nativeSearchQueryBuilder.withSort(sortBuilder);
                }
            });
        }
        nativeSearchQueryBuilder.withPageable(PageRequest.of(number, size));
        NativeSearchQuery searchQuery = nativeSearchQueryBuilder.build();
        log.debug("\r\n ES 查询语句： \r\n {} \r\n", genSearch(searchQuery));
        // 这个版本获取总数最大只能获取到 10000，这里先统计一下先
        long count = elasticsearchOperations.count(searchQuery, clazz);
        if (count <= 0) {
            return new PageImpl(Collections.emptyList(), searchQuery.getPageable(), count);
        }
        // 设置查询真实总数
        searchQuery.setTrackTotalHits(true);
        SearchHits<E> searchHits = elasticsearchOperations.search(searchQuery, clazz);
        SearchPage<E> searchPage = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
        // return (Page) SearchHitSupport.unwrapSearchHits(searchPage);
        List<?> content = (List) SearchHitSupport.unwrapSearchHits(searchPage.getSearchHits());
        assert content != null;
        return new PageImpl(content, searchPage.getPageable(), count);
    }

    /**
     * 查询语句分页查询
     * <br> 官方文档
     * <a href="https://docs.spring.io/spring-data/elasticsearch/docs/4.3.10/reference/html/#elasticsearch.operations.stringquery">StringQuery</a>
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
        log.debug("\r\n ES 查询语句： \r\n {} \r\n", stringQuery.getSource());
        // 设置查询真实总数
        stringQuery.setTrackTotalHits(true);
        SearchHits<E> searchHits = elasticsearchOperations.search(stringQuery, clazz);
        SearchPage<E> page = SearchHitSupport.searchPageFor(searchHits, stringQuery.getPageable());
        return (Page) SearchHitSupport.unwrapSearchHits(page);
    }

}
