package io.github.mangocrisp.spring.taybct.tool.core.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PrimitiveArrayUtil;
import cn.hutool.extra.expression.engine.spel.SpELEngine;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldDefault;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableFieldJSON;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.TableLogicUnique;
import io.github.mangocrisp.spring.taybct.tool.core.config.DefaultTableFieldDefaultHandler;
import io.github.mangocrisp.spring.taybct.tool.core.config.TableFieldDefaultHandler;
import io.github.mangocrisp.spring.taybct.tool.core.constant.PageRequestConstants;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.MybatisOptional;
import io.github.mangocrisp.spring.taybct.tool.core.request.SqlQueryParams;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.postgresql.util.PGobject;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 原 get 请求需要分页的工具类 GetPageUtil
 * 这个工具类，如果是为了方便 Mybatis 的一些操作
 *
 * @author xijieyin <br> 2022/8/5 18:42
 * @since 1.0.0
 */
@Slf4j
public class MyBatisUtil {

    /**
     * 字段可选配置
     *
     * @param <T> 类型
     * @return 字段可选配置类
     */
    public static <T> MybatisOptional<T> mybatisOptional() {
        return new MybatisOptional<>();
    }

    /**
     * 以前的接口还是能用
     *
     * @param params 请求参数
     * @param <T>    需要的类型
     * @return 分页
     * @author xijieyin <br> 2022/8/5 18:42
     * @since 1.0.0
     */
    public static <T> Page<T> genPage(Map<String, Object> params) {
        return genPage(params, false);
    }

    /**
     * 以前的接口还是能用
     *
     * @param params           请求参数
     * @param clearQueryParams 是否同时清理分页参数
     * @param <T>              需要的类型
     * @return 分页
     * @author xijieyin <br> 2022/8/5 18:42
     * @since 3.1.0
     */
    public static <T> Page<T> genPage(Map<String, Object> params, boolean clearQueryParams) {
        Page<T> page = genPage(new SqlQueryParams(Convert.toLong(params.get(PageRequestConstants.PAGE_NUM))
                , Convert.toLong(params.get(PageRequestConstants.PAGE_SIZE))
                , Convert.toStr(params.get(PageRequestConstants.ORDER_BY_COLUMN))));
        if (clearQueryParams) {
            clearQueryParams(params);
        }
        return page;
    }

    /**
     * 根据传入的参数构建分页
     *
     * @param pageParams 分页参数
     * @return {@code Page<T>}
     * @author xijieyin
     * @since 2.3.0
     */
    public static <T> Page<T> genPage(SqlQueryParams pageParams) {
        Assert.notNull(pageParams, "请求参数不能为空");
        Page<T> page = new Page<>();
        // 当前页码
        page.setCurrent(Optional.ofNullable(pageParams.getPageNum()).orElse(page.getCurrent()));
        // 页面大小
        page.setSize(Optional.ofNullable(pageParams.getPageSize()).orElse(page.getSize()));
        // 排序
        Optional.ofNullable(getPageOrder(pageParams.getPageOrder()))
                // 先按 "," 分开
                .map(order -> order.split(PageRequestConstants.FIELD_SEPARATE))
                // 然后按 " " 分开
                .ifPresent(sortArray -> Arrays.stream(sortArray).forEach(s -> {
                    String[] sa = s.split(" ");
                    if (sa.length > 1) {
                        page.orders().add(sa[1].equalsIgnoreCase("asc") ? OrderItem.asc(sa[0]) : OrderItem.desc(sa[0]));
                    } else {
                        page.orders().add(OrderItem.asc(sa[0]));
                    }
                }));
        return page;
    }

    /**
     * @param params 传入的参数
     * @param clazz  实体类类型
     * @return {@code Wrapper<T>}
     * @author xijieyin <br> 2022/8/5 18:43
     * @since 1.0.0
     */
    public static <T> Wrapper<T> genQueryWrapper(Map<String, Object> params, Class<T> clazz) {
        return genQueryWrapper(JSONObject.parseObject(JSONObject.toJSONString(params)).toJavaObject(clazz)
                , new SqlQueryParams(Convert.toLong(params.get(PageRequestConstants.PAGE_NUM))
                        , Convert.toLong(params.get(PageRequestConstants.PAGE_SIZE))
                        , Convert.toStr(params.get(PageRequestConstants.ORDER_BY_COLUMN))));
    }

    /**
     * @param pageParams 分页参数
     * @param dto        实体类
     * @return {@code Wrapper<T>}
     * @author xijieyin
     * @since 2.3.0
     */
    public static <T> Wrapper<T> genQueryWrapper(T dto, SqlQueryParams pageParams) {
        // 排序字段逗号隔开了
        QueryWrapper<T> wrapper = new QueryWrapper<>(dto);
        if (pageParams != null) {
            String orderStr = Convert.toStr(pageParams.getPageOrder());
            if (ObjectUtil.isNotEmpty(orderStr)) {
                // 逗号隔开条件
                Arrays.asList(orderStr.split(PageRequestConstants.FIELD_SEPARATE)).forEach(orders -> {
                    // 空格隔开字段
                    String[] fieldOrder = orders.split(PageRequestConstants.ORDER_SEPARATE);
                    String field = fieldOrder[0];
                    if (fieldOrder.length > 1) {
                        String order = fieldOrder[1];
                        if (order.equalsIgnoreCase("asc")) {
                            wrapper.orderByAsc(field);
                        } else {
                            wrapper.orderByDesc(field);
                        }
                    } else {
                        // 默认正序
                        wrapper.orderByAsc(field);
                    }
                });
            }
        }
        return wrapper;
    }


    /**
     * 获取到分页信息
     *
     * @param params 请求参数
     * @return int[0] 页码 int[1] 分页大小
     * @author xijieyin <br> 2022/8/5 14:38
     * @since 1.0.0
     */
    public static int[] getPageInfo(Map<String, Object> params) {
        return new int[]{Convert.toInt(params.get(PageRequestConstants.PAGE_NUM), 1)
                , Convert.toInt(params.get(PageRequestConstants.PAGE_SIZE), 10)};
    }

    /**
     * 获取到排序字段，并且清除分页数据
     *
     * @param pageParams sql 查询对象
     * @return 排序
     * @author xijieyin
     * @since 2.3.0
     */
    public static String getPageOrder(SqlQueryParams pageParams) {
        return getPageOrder(pageParams.getPageOrder());
    }

    /**
     * 获取到排序字段，并且清除分页数据
     *
     * @param params 查询参数
     * @return 排序
     * @author xijieyin <br> 2022/8/5 14:38
     * @since 1.0.0
     */
    public static String getPageOrder(Map<String, Object> params) {
        return getPageOrder(Convert.toStr(params.get(PageRequestConstants.ORDER_BY_COLUMN)));
    }

    /**
     * 获取到排序字段，并且清除分页数据
     *
     * @param orderStr 排序字段
     * @return String
     * @author xijieyin
     * @since 2.3.0
     */
    public static String getPageOrder(String orderStr) {
        if (StringUtil.isEmpty(orderStr)) {
            return null;
        }
        String[] fields =
                // 把 ;,(,) 替换掉，防止 sql 注入
                orderStr.replaceAll(";", "")
                        .replaceAll("\\(", "")
                        .replaceAll("\\)", "")
                        // 按逗号隔开
                        .split(PageRequestConstants.FIELD_SEPARATE);
        List<String> ascCollection = Arrays.stream(fields)
                .map(StringUtil::humpToUnderline)
                .map(String::trim)
                .toList();
        // 把结果丢到一个 Set 里面去去重
        return CollectionUtil.join(new LinkedHashSet<>(ascCollection), PageRequestConstants.FIELD_SEPARATE);
    }

    /**
     * 清理查询信息，清理掉一些查询信息，如分页，排序等，这些不是表的字段，但是在查询参数里面的信息
     * 所以，最好不要用这三个字段，作为你的表设计里面的表字段：<br>
     * {@link PageRequestConstants#PAGE_NUM}<br>
     * {@link PageRequestConstants#PAGE_SIZE}<br>
     * {@link PageRequestConstants#ORDER_BY_COLUMN}
     *
     * @param params 请求参数
     * @author xijieyin <br> 2022/8/5 14:38
     * @since 1.0.0
     */
    public static void clearQueryParams(Map<String, Object> params) {
        params.remove(PageRequestConstants.PAGE_NUM);
        params.remove(PageRequestConstants.PAGE_SIZE);
        params.remove(PageRequestConstants.ORDER_BY_COLUMN);
    }

    /**
     * 请求参数驼峰转下划线
     *
     * @param params 请求参数
     * @return {@code Map<Object>}
     * @author xijieyin <br> 2022/8/5 14:41
     * @since 1.0.0
     */
    public static Map<String, Object> humpToUnderline(Map<String, Object> params) {
        Map<String, Object> converted = new HashMap<>();
        params.forEach((key, value) -> converted.put(StringUtil.humpToUnderline(key).toLowerCase(), value));
        return converted;
    }

    /**
     * 为做新增操作的数据设置默认值
     *
     * @param domains 实体类对象
     * @author xijieyin <br> 2022/10/25 11:28
     * @since 1.0.5
     */
    public static <T> void setInsertDefaultValue(Collection<T> domains) {
        domains.forEach(entity -> {
            Class<?> clazz = entity.getClass();
            for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            Object fieldValue = field.get(entity);
                            if (field.isAnnotationPresent(TableFieldDefault.class) && ObjectUtil.isEmpty(fieldValue)) {
                                Object defaultValue = getDefaultValue(entity, field);
                                if (ObjectUtil.isNotEmpty(defaultValue)) {
                                    field.set(entity, defaultValue);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
    }

    /**
     * mybatis 拦截器获取方法
     *
     * @param mappedStatement 拦截器的参数
     * @return Method
     */
    public static Method getMethod(MappedStatement mappedStatement) {
        String id = mappedStatement.getId();
        // 获取 Class Method
        String clazzName = id.substring(0, id.lastIndexOf('.'));
        String mapperMethod = id.substring(id.lastIndexOf('.') + 1);

        Class<?> clazz;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            return null;
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(mapperMethod)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 检查是否需要拦截默认值设置
     *
     * @param params 参数
     * @return boolean
     */
    public static boolean checkTableFieldDefault(Object params) {
        if (params != null) {
            if (params instanceof MapperMethod.ParamMap) {
                for (Object o : ((Map) params).values()) {
                    if (o != null && !BeanUtils.isSimpleValueType(o.getClass())) {
                        if (checkTableFieldDefault(o)) return true;
                    }
                }
                return false;
            } else if (BeanUtils.isSimpleValueType(params.getClass())) {
                return false;
            } else if (params instanceof Collection) {
                for (Object o : (Collection) params) {
                    if (checkTableFieldDefault(o)) {
                        return true;
                    }
                }
            } else {
                List<Field> fields = new ArrayList<>();
                BeanUtil.getAllFields(fields, params.getClass());
                for (Field field : fields) {
                    if (field.isAnnotationPresent(TableFieldDefault.class)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取到访问方法的参数列表
     *
     * @param params 拦截参数
     * @param ms     调用的方法
     * @return 参数 map
     */
    @NotNull
    public static Map<String, Object> getParamMap(MappedStatement ms, Object params) {
        return getParamMap(ms, params, false);
    }

    /**
     * 获取到访问方法的参数列表
     *
     * @param params     拦截参数
     * @param ms         调用的方法
     * @param setDefault 是否设置默认值，如果字段没有值，会根据 {@linkplain TableFieldDefault @TableFieldDefault } 注解来设置默认值，如果有值就还是字段本来的值
     * @return 参数 map
     */
    @NotNull
    public static Map<String, Object> getParamMap(MappedStatement ms, Object params, boolean setDefault) {
        return getParamMap(ms, params, setDefault, null, false);
    }

    /**
     * 获取到访问方法的参数列表
     *
     * @param ms           调用的方法
     * @param params       拦截参数
     * @param setDefault   是否设置默认值，如果字段没有值，会根据 {@linkplain TableFieldDefault @TableFieldDefault } 注解来设置默认值，如果有值就还是字段本来的值
     * @param paramMap     存放最终值的 map
     * @param isCollection 是否是集合
     * @return 参数 map
     */
    @NotNull
    public static Map<String, Object> getParamMap(MappedStatement ms
            , Object params
            , boolean setDefault
            , Map<String, Object> paramMap
            , boolean isCollection) {
        if (paramMap == null) {
            paramMap = new MapperMethod.ParamMap<>();
        }
        if (params != null) {
            Method method = getMethod(ms);
            Parameter[] parameters = method.getParameters();
            String paramName = parameters[0].getName();
            if (params instanceof MapperMethod.ParamMap) {
                for (Object o : ((Map) params).values()) {
                    if (o != null && !BeanUtils.isSimpleValueType(o.getClass())) {
                        getParamMap(ms, o, setDefault, paramMap, true);
                    }
                }
                if (!isCollection) {
                    paramMap.putAll((Map) params);
                }
            } else {
                Class<?> cls = params.getClass();
                if (BeanUtils.isSimpleValueType(cls)) {
                    if (!isCollection) {
                        paramName = checkMybatisPlusBaseMapper(ms, method, paramName);
                        paramMap.put(paramName, params);
                    }
                } else if (params instanceof IPage) {
                    // 兼容 MyBatisPlus 的分页参数
                    paramMap.put(paramName, params);
                } else if (params instanceof Collection) {
                    for (Object o : (Collection) params) {
                        getParamMap(ms, o, setDefault, paramMap, true);
                    }
                    if (!isCollection) {

                        // 如果是集合的话，我只能把默认值设置进去
                        paramMap.put(paramName, params);
                    }
                } else {
                    // 操作类型
                    SqlCommandType sqlCommandType = ms.getSqlCommandType();
                    // 是否是更新操作
                    boolean isUpdate = sqlCommandType.equals(SqlCommandType.UPDATE);
                    List<Field> fields = new ArrayList<>();
                    BeanUtil.getAllFields(fields, cls);
                    Method[] methods = cls.getMethods();
                    // 主键类型
                    Class<?> pkClass = null;
                    // 主键
                    String pkKey = null;
                    // 主键值
                    Object pkVal = null;
                    // 唯一逻辑删除键
                    String tableLogicUniqueKey = null;
                    // 检查是否有这个逻辑唯一键
                    boolean hasTableLogicUniqueKey = false;
                    // 是否需要设置逻辑唯一键
                    boolean willSetTableLogicUniqueKey = false;
                    if (cls.isAnnotationPresent(TableLogicUnique.class)) {
                        TableLogicUnique annotation = cls.getAnnotation(TableLogicUnique.class);
                        tableLogicUniqueKey = annotation.value();
                    }
                    for (Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            // 如果是静态字段，就不管
                            continue;
                        }
                        Object fieldValue = null;
                        String fieldName = field.getName();
                        if (StringUtil.isNotBlank(tableLogicUniqueKey) && tableLogicUniqueKey.equals(fieldName)) {
                            hasTableLogicUniqueKey = true;
                        }
                        try {
                            Method readMethod = (new PropertyDescriptor(fieldName, cls)).getReadMethod();
                            fieldValue = readMethod.invoke(params);
                        } catch (Exception e) {
                            log.trace(e.getMessage(), e);
                        }
                        field.setAccessible(true);
                        try {
                            if (fieldValue == null) {
                                fieldValue = field.get(params);
                            }
                        } catch (Exception e) {
                            log.trace(e.getMessage(), e);
                        }
                        if (fieldValue == null) {
                            String firstUpper = StringUtil.upperFirst(fieldName);
                            List<Method> methodList = Arrays.stream(methods).filter(m -> m.getName().equals("is" + firstUpper)
                                            || m.getName().equals("get" + firstUpper))
                                    .collect(Collectors.toList());
                            Method readMethod;
                            if (CollectionUtil.isNotEmpty(methodList) && (readMethod = methodList.get(0)) != null) {
                                try {
                                    fieldValue = readMethod.invoke(params);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    log.trace(e.getMessage(), e);
                                }
                            }
                        }
                        if (setDefault && field.isAnnotationPresent(TableFieldDefault.class) && ObjectUtil.isEmpty(fieldValue)) {
                            fieldValue = getDefaultValue(params, field, ms);
                            // 这里拿到默认值后，把值设置进去
                            if (ObjectUtil.isNotEmpty(fieldValue)) {
                                try {
                                    (new PropertyDescriptor(fieldName, cls)).getWriteMethod().invoke(params, fieldValue);
                                } catch (IllegalAccessException | InvocationTargetException |
                                         IntrospectionException e) {
                                    log.trace(e.getMessage(), e);
                                }
                            }
                        }
                        if (field.isAnnotationPresent(TableFieldJSON.class)) {
                            // 如果是表 json 类型字段处理
                            fieldValue = getJSONFieldValue(ms, fieldValue);
                            // 这里拿到默认值后，把值设置进去
                            if (ObjectUtil.isNotEmpty(fieldValue)) {
                                try {
                                    (new PropertyDescriptor(fieldName, cls)).getWriteMethod().invoke(params, fieldValue);
                                } catch (IllegalAccessException | InvocationTargetException |
                                         IntrospectionException e) {
                                    log.trace(e.getMessage(), e);
                                }
                            }
                        }
                        // 如果是更新操作，而且不是空值的逻辑键
                        if (field.isAnnotationPresent(TableLogic.class) && isUpdate && ObjectUtil.isNotEmpty(fieldValue)) {
                            if (SpringUtil.getContext() != null) {
                                String logicDeleteValue = SpringUtil.getContext()
                                        .getEnvironment().getProperty("mybatis-plus.global-config.db-config.logic-delete-value");
                                // 如果更新的值是逻辑删除的值，这里就要更新
                                willSetTableLogicUniqueKey = Convert.toStr(fieldValue).equals(logicDeleteValue);
                            }
                        }
                        if (field.isAnnotationPresent(TableId.class) && isUpdate && ObjectUtil.isNotEmpty(fieldValue)) {
                            pkClass = field.getType();
                            pkKey = fieldName;
                            pkVal = fieldValue;
                        }
                        // 如果是集合对象的话，就不需要设置这些值
                        if (!isCollection) {
                            paramMap.putIfAbsent(fieldName, fieldValue);
                        }
                    }
                    if (hasTableLogicUniqueKey && willSetTableLogicUniqueKey && ObjectUtil.isNotNull(pkVal)) {
                        paramMap.putIfAbsent(tableLogicUniqueKey, pkVal);
                    }
                }
            }
        }
        return paramMap;
    }

    /**
     * 检查是否是 MyBatisPlus 的 BaseMapper 里面的方法，如果是的话，返回正确的字段名
     *
     * @param ms        方法
     * @param method    方法
     * @param paramName 原字段名
     * @return 字段名
     */
    private static String checkMybatisPlusBaseMapper(MappedStatement ms, Method method, String paramName) {
        // 判断获取到的方法是不是 Mybatis plus 的 BaseMapper 里面的方法，如果是的话，那所有的使用 id 的操作都是简单类型的，但是如果 id 和实际不一样的话，得要看实体类里面具体是什么值
        if (method.getDeclaringClass().equals(BaseMapper.class)) {
            String clazzName = ms.getId().substring(0, ms.getId().lastIndexOf('.'));
            try {
                Class<?>[] classes = GenericTypeUtils.resolveTypeArguments(Class.forName(clazzName), BaseMapper.class);
                if (null != classes) {
                    // 这里去找到继承 BaseMapper 的形参的类型来获取类型里面所有的字段，有 @TableId 的字段就可以知道是 @TableId 了
                    List<Field> fields = new ArrayList<>();
                    BeanUtil.getAllFields(fields, classes[0]);
                    for (Field field : fields) {
                        // 这边拿到第一个就算
                        if (field.isAnnotationPresent(TableId.class)) {
                            return field.getName();
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log.trace(e.getMessage(), e);
                // 如果报错，就直接用字段名
            }
        }
        return paramName;
    }

    /**
     * 获取 JSON 字段的值
     *
     * @param ms         调用的方法
     * @param fieldValue 字段值
     * @return 最终结果
     */
    public static Object getJSONFieldValue(MappedStatement ms, Object fieldValue) {
        DbType dbType = DbType.getDbType(ms.getConfiguration().getDatabaseId());
        if (Objects.requireNonNull(dbType) == DbType.POSTGRE_SQL) {
            // 如果是 pgsql 需要做额外处理
            PGobject pGobject = new PGobject();
            pGobject.setType("json");
            try {
                if (fieldValue instanceof JSONArray) {
                    pGobject.setValue(((JSONArray) fieldValue).toJSONString());
                }
                if (fieldValue instanceof JSONObject) {
                    pGobject.setValue(((JSONObject) fieldValue).toJSONString());
                }
                if (fieldValue instanceof String) {
                    pGobject.setValue((String) fieldValue);
                }
                fieldValue = pGobject;
            } catch (SQLException e) {
                log.trace(e.getMessage(), e);
            }
        }
        // 后面再添加其他数据库的兼容，这里先兼容 pgsql
        return fieldValue;
    }


    /**
     * 获取字段上的默认值
     *
     * @param entity 实体类
     * @param field  字段
     * @return 默认值
     */
    @SneakyThrows
    public static Object getDefaultValue(Object entity, Field field) {
        return getDefaultValue(entity, field, null);
    }

    /**
     * 获取字段上的默认值
     *
     * @param entity 实体类
     * @param field  字段
     * @param ms     调用的方法
     * @return 默认值
     */
    @SneakyThrows
    public static Object getDefaultValue(Object entity, Field field, MappedStatement ms) {
        Class<?> type = field.getType();
        TableFieldDefault annotation = field.getAnnotation(TableFieldDefault.class);
        if (ms != null) {
            SqlCommandType sqlCommandType = ms.getSqlCommandType();
            // 根据指定的数据库操作类型来判断是否要填充默认值
            if (ArrayUtil.indexOf(annotation.fill(), sqlCommandType) == PrimitiveArrayUtil.INDEX_NOT_FOUND) {
                // 如果在数组里面找不到，说明不是对应的要操作的方法
                return null;
            }
        }
        String defaultValue = annotation.value();
        // 行内处理的 SpEL 表达式是最高优先级
        if (StringUtil.isNotBlank(annotation.expression())) {
            return Convert.convert(type, new SpELEngine().eval(annotation.expression(), Collections.emptyMap(), Collections.emptySet()));
        }
        // 然后是自定义的默认值处理器
        if (!annotation.handler().equals(DefaultTableFieldDefaultHandler.class)) {
            TableFieldDefaultHandler bean = SpringUtil.getBean(annotation.handler());
            return Optional.ofNullable(bean)
                    .map(b -> b.get(entity))
                    .orElse(null);
        }
        // 如果原值为空，默认值不为空就设置成默认值
        if (StringUtil.isNotEmpty(defaultValue)) {
            return Convert.convert(type, defaultValue);
        }
        // 如果确定是要设置一个空字符串
        if (annotation.isBlank()) {
            return "";
        }
        // 如果是日期类型，这里是要设置当前时间
        if (annotation.isTimeNow()) {
            return LocalDateTime.now();
        }
        // 设置随机值
        if (annotation.isRandom()) {
            return IdWorker.get32UUID();
        }
        return null;
    }

}
