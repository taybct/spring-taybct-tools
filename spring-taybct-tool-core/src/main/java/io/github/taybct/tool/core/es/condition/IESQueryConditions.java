package io.github.taybct.tool.core.es.condition;


import cn.hutool.core.util.StrUtil;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * ES 查询条件，你可以选择实现我的这个接口，然后写自己的条件拼装，我这里目前只写了一个 termQuery，大致就是所有的 @Field 或者 @Id 注解的字段会被套用 termQuery
 *
 * @author xijieyin
 */
public interface IESQueryConditions<T> {

    /**
     * 拼装一个 BoolQuery
     *
     * @param entity       实体
     * @param queryBuilder boolQueryBuilder 可以先声明好
     */
    default BoolQuery.Builder assembleBoolQuery(T entity, BoolQuery.Builder queryBuilder) {
        Assert.notNull(entity, "请求实体不能为空！");
        Class<?> clazz = entity.getClass();
        if (entity instanceof Map map) {
            map.forEach((k, v) -> queryBuilder.must(b -> b.term(m -> m.field(k.toString()).value(v.toString()))));
            return queryBuilder;
        }
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
                            String finalName = name;
                            if (type.equals(FieldType.Text)) {
                                queryBuilder.must(b -> b.match(m -> m.field(finalName).query(value.toString())));
                            } else {
                                queryBuilder.must(b -> b.term(t -> t.field(finalName).value(value.toString())));
                            }
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return queryBuilder;
    }
}
