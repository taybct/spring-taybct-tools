package io.github.taybct.tool.core.mybatis.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.support.IdeaProxyLambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.ShadowLambdaMeta;
import io.github.taybct.tool.core.annotation.TableFieldJSON;
import io.github.taybct.tool.core.bean.BaseEntity;
import io.github.taybct.tool.core.mybatis.constant.Constants;
import io.github.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.taybct.tool.core.util.StringPool;
import io.github.taybct.tool.core.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <pre>
 * MyBatis 可选项
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/17 19:26
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class MybatisOptional<T> {

    /**
     * 更新用户的实体类数据对象
     */
    private T bean;
    /**
     * 查询参数（where 条件）
     */
    private JSONObject params = new JSONObject();
    /**
     * 分页参数（limit 条件）
     */
    private SqlPageParams page = new SqlPageParams();
    /**
     * 最后选择好的字段列表
     */
    final LinkedHashSet<String> selectedFieldSet = new LinkedHashSet<>();
    /**
     * 查询条件，直接写 SQL 的 where 条件（只建议后端写接口用，不要开出去给前端调用）
     */
    final ConcurrentHashMap<String, String> conditions = new ConcurrentHashMap<>();

    /**
     * 设置当前的 bean
     *
     * @param bean bean
     * @return 当前
     */
    public MybatisOptional<T> bean(T bean) {
        this.bean = bean;
        return this;
    }

    /**
     * 获取到所有的字段，按 {@linkplain Constants#DOT} 隔开
     *
     * @return 所选的字段
     */
    public String getSelectedFields() {
        return CollectionUtil.join(this.selectedFieldSet, Constants.COMMA);
    }

    /**
     * 正序排序
     *
     * @param columns 排序字段
     * @return 当前
     * @throws Exception 异常
     */
    @SafeVarargs
    public final MybatisOptional<T> orderByASC(SFunction<T, ?>... columns) throws Exception {
        return orderBy(true, columns);
    }

    /**
     * 倒序排序
     *
     * @param columns 排序字段
     * @return 当前
     * @throws Exception 异常
     */
    @SafeVarargs
    public final MybatisOptional<T> orderByDESC(SFunction<T, ?>... columns) throws Exception {
        return orderBy(false, columns);
    }

    /**
     * 排序
     *
     * @param isAsc   是否正序
     * @param columns 排序字段
     * @return 当前
     * @throws Exception 异常
     */
    @SafeVarargs
    public final MybatisOptional<T> orderBy(boolean isAsc, SFunction<T, ?>... columns) throws Exception {
        optionalField(CollectionUtil.toList(columns)
                , false
                , fields -> orderBy(fields.stream()
                        .map(field -> new OrderByField(field, isAsc ? Constants.ASC : Constants.DESC)).toList()));
        return this;
    }

    /**
     * 排序
     *
     * @param orderByFields 需要排序的字段
     * @return 当前
     */
    public MybatisOptional<T> orderBy(List<OrderByField> orderByFields) {
        if (CollectionUtil.isNotEmpty(orderByFields)) {
            LinkedHashSet<String> sort = this.page.getSort();
            if (CollectionUtil.isEmpty(sort)) {
                sort = new LinkedHashSet<>();
            }
            sort.addAll(orderByFields.stream()
                    .filter(f -> StringUtil.isNotBlank(f.getField()))
                    .peek(f -> {
                        if (StringUtil.isBlank(f.getSc())) {
                            f.setSc(Constants.ASC);
                        }
                    })
                    .map(f -> String.format("%s %s", f.field, f.sc)).toList());
            this.page.setSort(sort);
        }
        return this;
    }

    /**
     * 分页
     *
     * @param pageSize 分页大小
     * @return 当前
     */
    public MybatisOptional<T> page(long pageSize) {
        return page(pageSize, 0);
    }

    /**
     * 分页
     *
     * @param pageSize 分页大小
     * @param pageNum  页码
     * @return 当前
     */
    public MybatisOptional<T> page(long pageSize, long pageNum) {
        this.page.setPageSize(pageSize);
        this.page.setPageNum(pageNum);
        return this;
    }

    /**
     * 分页设置
     *
     * @param page 分页
     * @return 当前
     */
    public MybatisOptional<T> page(SqlPageParams page) {
        this.page = page;
        return this;
    }

    /**
     * 同 JSONObject.from，不同的是可以往里面加数据，也可以直接替换
     *
     * @param e   查询参数对象
     * @param <E> 类型
     * @return 当前
     */
    public <E extends Serializable> MybatisOptional<T> fromCondition(Supplier<E> e) {
        return from(e.get());
    }

    /**
     * 同 JSONObject.from，不同的是可以往里面加数据，也可以直接替换
     *
     * @param e   查询参数对象
     * @param <E> 类型
     * @return 当前
     */
    public <E extends Serializable> MybatisOptional<T> from(E e) {
        return from(e, false);
    }

    /**
     * 同 JSONObject.from，不同的是可以往里面加数据，也可以直接替换
     *
     * @param e       查询参数对象
     * @param replace 是否要替换已有的参数
     * @param <E>     类型
     * @return 当前
     */
    public <E extends Serializable> MybatisOptional<T> from(E e, boolean replace) {
        return params((JSONObject) JSON.toJSON(e), replace);
    }

    /**
     * 设置请求参数
     *
     * @param params 查询参数 JSON
     * @return 当前
     */
    public MybatisOptional<T> params(JSONObject params) {
        return params(params, false);
    }

    /**
     * 设置请求参数
     *
     * @param params  查询参数 JSON
     * @param replace 是否要替换已有的参数
     * @return 当前
     */
    public MybatisOptional<T> params(JSONObject params, boolean replace) {
        if (replace) {
            this.params = params;
            return this;
        }
        this.params.putAll(params);
        return this;
    }

    /**
     * 设置请求参数
     *
     * @param key   参数 key
     * @param value 参数值
     * @param <V>   参数类型
     * @return 当前
     */
    public <V extends Serializable> MybatisOptional<T> params(String key, V value) {
        this.params.put(key, value);
        return this;
    }

    /**
     * 设置 where 条件
     *
     * @param condition where 条件语句
     * @return 当前
     */
    public MybatisOptional<T> where(Supplier<String> condition) {
        return where(condition.get());
    }

    /**
     * 设置 where 条件
     *
     * @param place     where 的位置
     * @param condition where 条件语句
     * @return 当前
     */
    public MybatisOptional<T> where(String place, Supplier<String> condition) {
        return where(place, condition.get());
    }

    /**
     * 设置 where 条件
     *
     * @param condition where 条件语句
     * @return 当前
     */
    public MybatisOptional<T> where(String condition) {
        return where(Constants.CONDITION_PLACE_BASIC, condition);
    }

    /**
     * 设置 where 条件
     *
     * @param place     where 的位置
     * @param condition where 条件语句
     * @return 当前
     */
    public MybatisOptional<T> where(String place, String condition) {
        if (StringUtil.isNotBlank(place) && StringUtil.isNotBlank(condition)) {
            this.conditions.put(place, condition);
        }
        return this;
    }

    /**
     * 查询哪些字段（当前表）
     *
     * @param columns 字段列表
     * @return 字段集合
     * @throws Exception 异常
     */
    @SafeVarargs
    public final MybatisOptional<T> select(SFunction<T, ?>... columns) throws Exception {
        optionalField(CollectionUtil.toList(columns), true, this.selectedFieldSet::addAll);
        return this;
    }

    /**
     * 查询哪些字段
     *
     * @param fields 字段
     * @return 当前
     */
    public MybatisOptional<T> select(String... fields) {
        return select(CollectionUtil.toList(fields));
    }

    /**
     * 查询哪些字段
     *
     * @param fields 字段
     * @return 当前
     */
    public MybatisOptional<T> select(Supplier<Collection<String>> fields) {
        return select(fields.get());
    }

    /**
     * 查询哪些字段
     *
     * @param fields 字段
     * @return 当前
     */
    public MybatisOptional<T> select(Collection<String> fields) {
        if (CollectionUtil.isNotEmpty(fields)) {
            this.selectedFieldSet.addAll(fields);
        }
        return this;
    }

    /**
     * 查询哪些字段（关联表）
     *
     * @param columns 字段列表
     * @param <R>     关联表的类型
     * @return 字段集合
     * @throws Exception 异常
     */
    @SafeVarargs
    public final <R> MybatisOptional<T> association(SFunction<R, ?>... columns) throws Exception {
        optionalField(CollectionUtil.toList(columns), true, this.selectedFieldSet::addAll);
        return this;
    }

    /**
     * 获取需要查询的字段
     *
     * @param columns       字段列表
     * @param withTableName 是否需要带上表名例如，a 表的 b 字段 会组合成 a_b
     * @param fieldConsumer 处理得到的字段集合
     * @throws Exception 异常
     */
    public <R> void optionalField(Collection<SFunction<R, ?>> columns
            , boolean withTableName
            , Consumer<LinkedHashSet<String>> fieldConsumer) throws Exception {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Map<Class<?>, Map<String, List<String>>> classFieldMaps = new ConcurrentHashMap<>();
        LinkedHashSet<Class<?>> classLinkedHashSet = new LinkedHashSet<>();
        extract(columns, classLinkedHashSet, classFieldMaps);
        classLinkedHashSet.forEach(clazz -> {
            try {
                result.addAll(optionalField(clazz, withTableName, classFieldMaps.get(clazz)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        fieldConsumer.accept(result);
    }

    /**
     * 按类处理获取到需要操作哪些字段
     *
     * @param columns            字段列表
     * @param classLinkedHashSet 类型列表
     * @param classFieldMaps     类型匹配字段
     * @param <R>                表类型
     */
    public static <R> void extract(Collection<SFunction<R, ?>> columns
            , LinkedHashSet<Class<?>> classLinkedHashSet
            , Map<Class<?>, Map<String, List<String>>> classFieldMaps) {
        for (SFunction<R, ?> column : columns) {
            LambdaMeta meta = extract(column);
            Class<?> clazz = meta.getInstantiatedClass();
            if (clazz == null) {
                throw new IllegalStateException("clazz is null.");
            }
            try {
                Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
                Object temp = declaredConstructor.newInstance();
                Object apply = column.apply((R) temp);
                if (apply instanceof Collection<?> collection) {
                    try {
                        extract((Collection<SFunction<R, ?>>) collection, classLinkedHashSet, classFieldMaps);
                        return;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
            classLinkedHashSet.add(clazz);
            Map<String, List<String>> fieldMaps = classFieldMaps.computeIfAbsent(clazz, k -> new LinkedHashMap<>());
            if (meta instanceof ReflectLambdaMeta reflectLambdaMeta) {
                String key = reflectLambdaMeta.getLambda().getImplClass().replaceAll(StringPool.SLASH, StringPool.DOT);
                List<String> list = fieldMaps.getOrDefault(key, new ArrayList<>());
                list.add(fieldName);
                fieldMaps.put(key, list);
            }
            if (meta instanceof IdeaProxyLambdaMeta proxyLambdaMeta) {
                String key = proxyLambdaMeta.getInstantiatedClass().getName();
                List<String> list = fieldMaps.getOrDefault(key, new ArrayList<>());
                list.add(fieldName);
                fieldMaps.put(key, list);
            }
            classFieldMaps.put(clazz, fieldMaps);
        }
    }

    /**
     * 得到表达式相关信息
     *
     * @param func 方法
     * @param <R>  表类型
     * @return 相关信息
     */
    public static <R> LambdaMeta extract(SFunction<R, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return new ReflectLambdaMeta((SerializedLambda) method.invoke(func), func.getClass().getClassLoader());
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda.extract(func));
        }
    }

    /**
     * 获取需要查询的字段
     *
     * @param clazz         类
     * @param withTableName 是否需要带上表名例如，a 表的 b 字段 会组合成 a_b
     * @param fieldMaps     需要导出的字段集合
     * @return 字段集合
     * @throws Exception 异常
     */
    public static LinkedHashSet<String> optionalField(Class<?> clazz
            , boolean withTableName
            , Map<String, List<String>> fieldMaps) throws Exception {
        return optionalField(Set.of(BaseEntity.class.getPackage().getName())
                , clazz
                , withTableName
                , null
                , fieldMaps);
    }

    /**
     * 获取需要查询的字段
     *
     * @param supperEntityPackage 继承父类的包
     * @param clazz               类
     * @param withTableName       是否需要带上表名例如，a 表的 b 字段 会组合成 a_b
     * @param tableName           表名
     * @param fieldMaps           需要导出的字段集合
     * @return 字段集合
     * @throws Exception 异常
     */
    public static LinkedHashSet<String> optionalField(Set<String> supperEntityPackage
            , Class<?> clazz
            , boolean withTableName
            , String tableName
            , Map<String, List<String>> fieldMaps) throws Exception {
        LinkedHashSet<String> columns = new LinkedHashSet<>();
        for (Class<?> clazzFor = clazz; clazzFor != Object.class; clazzFor = clazzFor.getSuperclass()) {
            Field[] fields = clazzFor.getDeclaredFields();
            if (!supperEntityPackage.contains(clazzFor.getPackageName())) {
                // 表名
                tableName = StringUtil.humpToUnderline(clazzFor.getSimpleName());
                if (clazzFor.isAnnotationPresent(TableName.class)) {
                    tableName = clazzFor.getAnnotation(TableName.class).value();
                }
            }
            List<String> optionalFields = fieldMaps.get(clazzFor.getName());
            for (Field field : fields) {
                if (CollectionUtil.isEmpty(optionalFields) || CollectionUtil.isNotEmpty(optionalFields) &&
                        !optionalFields.contains(field.getName())) {
                    continue;
                }
                String realField = clazzFor.getName() + Constants.DOT + field.getName();
                String fieldName = StringUtil.humpToUnderline(field.getName());
                if (field.isAnnotationPresent(TableField.class)) {
                    TableField tableField = field.getAnnotation(TableField.class);
                    if (!tableField.exist()) {
                        continue;
                    }
                    String value = field.getAnnotation(TableField.class).value();
                    if (StringUtil.isNotEmpty(value)) {
                        fieldName = value;
                    }
                }
                if (field.isAnnotationPresent(TableId.class)) {
                    String value = field.getAnnotation(TableId.class).value();
                    if (StringUtil.isNotEmpty(value)) {
                        fieldName = value;
                    }
                }
                String key = withTableName ? tableName + Constants.UNDERSCORE + fieldName : fieldName;
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(TableField.class) || field.isAnnotationPresent(TableId.class)) {
                        columns.add(key);
                    }
                    if (field.getType().isAnnotationPresent(TableName.class)) {
                        LinkedHashSet<String> entityField = optionalField(supperEntityPackage, field.getType(), withTableName, tableName, fieldMaps);
                        if (CollectionUtil.isNotEmpty(entityField)) {
                            columns.addAll(entityField);
                        }
                    }
                    if (!field.isAnnotationPresent(TableFieldJSON.class)
                            && (field.getType().isAssignableFrom(List.class) || field.getType().isAssignableFrom(Set.class))) {
                        // 当前集合的泛型类型
                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            // 得到泛型里的class类型对象
                            Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                            LinkedHashSet<String> collectionField = optionalField(supperEntityPackage, actualTypeArgument, withTableName, tableName, fieldMaps);
                            if (CollectionUtil.isNotEmpty(collectionField)) {
                                columns.addAll(collectionField);
                            }
                        } else {
                            log.error("collection field {} type must be with generics type", realField);
                        }
                    }
                }
            }
        }
        return columns;
    }

}
