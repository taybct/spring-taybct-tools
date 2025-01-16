package io.github.mangocrisp.spring.taybct.tool.core.handle;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.mangocrisp.spring.taybct.tool.core.dto.SyncToAnywhereDTO;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.MybatisOptional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

/**
 * <pre>
 * mongoDB 处理器
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/13 15:28
 */
@RequiredArgsConstructor
public class SyncToMongoHandler<PK extends Serializable, T extends SyncToAnywhereDTO<PK>> implements SyncToAnywhereHandler<T, PK> {

    final MongoTemplate mongoTemplate;

    /**
     * id 字段名
     */
    @Setter
    private String idFieldName = "id";

    public SyncToMongoHandler(MongoTemplate mongoTemplate, String idFieldName) {
        this.mongoTemplate = mongoTemplate;
        this.idFieldName = idFieldName;
    }

    /**
     * 获取到指定的 id 字段
     *
     * @param clazz 数据类型
     * @return id 字段名
     */
    private String idField(Class<?> clazz) {
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Id.class)) {
                        return field.getName();
                    }
                }
            }
        }
        return idFieldName;
    }

    @Override
    public boolean insert(Collection<T> dtoCollection) {
        dtoCollection.stream().findFirst().ifPresentOrElse(dto -> mongoTemplate.insert(dtoCollection, dto.getClass())
                , () -> dtoCollection.forEach(mongoTemplate::save));
        return true;
    }

    @Override
    public boolean update(Collection<T> dtoCollection) {
        dtoCollection.stream().forEach(dto -> {
            Update update = new Update();
            Class<?> clazz = dto.getClass();
            Object id = dto.getId();
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            Object fieldValue = field.get(dto);
                            if (ObjectUtil.isNotEmpty(fieldValue)) {
                                update.set(field.getName(), fieldValue);
                            }
                            if (field.isAnnotationPresent(Id.class)) {
                                id = fieldValue;
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            mongoTemplate.updateFirst(new Query(Criteria.where(idField(dto.getClass())).is(id)), update, dto.getClass());
        });
        return true;
    }

    @Override
    public boolean delete(Collection<PK> dtoCollection, Class<T> convert) {
        mongoTemplate.remove(new Query(Criteria.where(idField(convert)).in(dtoCollection)), convert);
        return true;
    }

    @Override
    public IPage<T> page(MybatisOptional<T> mybatisOptional, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        SqlPageParams sqlPageParams = mybatisOptional.getPage();
        IPage<T> page = sqlPageParams.genPage();
        Query query = new Query();
        queryConditionHandler.where(query, mybatisOptional);
        if (sqlPageParams.getCountTotal()) {
            // 如果要统计数量
            long total = mongoTemplate.count(query, queryDataClass);
            page.setTotal(total);
        }
        queryConditionHandler.page(query, mybatisOptional);
        queryConditionHandler.sort(query, mybatisOptional);
        List<?> list = mongoTemplate.find(query, queryDataClass);
        if (CollectionUtil.isNotEmpty(list)) {
            page.setRecords(list.stream().map(e -> (T) e).toList());
        }
        return page;
    }

    @Override
    public long total(MybatisOptional<T> mybatisOptional, QueryConditionHandler<T> queryConditionHandler, Class<T> queryDataClass) {
        Query query = new Query();
        queryConditionHandler.where(query, mybatisOptional);
        return mongoTemplate.count(query, queryDataClass);
    }
}
