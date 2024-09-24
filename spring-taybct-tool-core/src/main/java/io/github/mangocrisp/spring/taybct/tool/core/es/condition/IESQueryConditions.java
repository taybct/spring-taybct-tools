package io.github.mangocrisp.spring.taybct.tool.core.es.condition;


import cn.hutool.core.util.StrUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.util.Assert;

import java.lang.reflect.Field;

/**
 * ES 查询条件，你可以选择实现我的这个接口，然后写自己的条件拼装，我这里目前只写了一个 termQuery，大致就是所有的 @Field 或者 @Id 注解的字段会被套用 termQuery
 *
 * @author xijieyin
 */
public interface IESQueryConditions<T> {

    /**
     * 拼装一个 BoolQuery
     *
     * @param entity           实体
     * @param boolQueryBuilder boolQueryBuilder 可以先声明好
     */
    default BoolQueryBuilder assembleBoolQuery(T entity, BoolQueryBuilder boolQueryBuilder) {
        if (boolQueryBuilder == null) {
            boolQueryBuilder = new BoolQueryBuilder();
        }
        Assert.notNull(entity, "请求实体不能为空！");
        Class<?> clazz = entity.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(org.springframework.data.elasticsearch.annotations.Field.class)
                        || field.isAnnotationPresent(org.springframework.data.annotation.Id.class)) {
                    try {
                        // es 查询的字段，默认直接是字段名
                        String name = field.getName();
                        Object value = field.get(entity);
                        // 字段类型
                        FieldType type = FieldType.Keyword;
                        if (field.isAnnotationPresent(org.springframework.data.elasticsearch.annotations.Field.class)) {
                            org.springframework.data.elasticsearch.annotations.Field annotation = field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
                            if (StrUtil.isNotBlank(annotation.value())) {
                                name = annotation.value();
                                type = annotation.type();
                            }
                        }
                        if (value != null) {
                            if (type.equals(FieldType.Text)) {
                                boolQueryBuilder.must(QueryBuilders.matchQuery(name, value));
                            } else {
                                boolQueryBuilder.must(QueryBuilders.termQuery(name, value));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return boolQueryBuilder;
    }
}
