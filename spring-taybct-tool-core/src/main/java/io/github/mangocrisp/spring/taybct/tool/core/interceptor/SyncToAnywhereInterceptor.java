package io.github.mangocrisp.spring.taybct.tool.core.interceptor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.SyncToAnywhere;
import io.github.mangocrisp.spring.taybct.tool.core.enums.SimpleDBOperateType;
import io.github.mangocrisp.spring.taybct.tool.core.handle.QueryConditionHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.SyncToAnywhereHandler;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.support.SqlPageParams;
import io.github.mangocrisp.spring.taybct.tool.core.mybatis.util.MybatisOptional;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <pre>
 * 同步拦截器
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/13 16:06
 */
@Slf4j
public class SyncToAnywhereInterceptor implements MethodInterceptor {

    static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class<?> returnType = invocation.getMethod().getReturnType();
        if (ObjectUtil.isEmpty(invocation.getArguments()) && returnType.equals(Void.class)) {
            // 如果没有参数而且返回类型是空，那就没必要做记录了
            return invocation.proceed();
        }
        // 获取到方法
        Method method = invocation.getMethod();
        // 获取到所有的参数名
        String[] parameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);
        // 获取到所有的参数
        Object[] args = invocation.getArguments();
        // 获取注解信息
        SyncToAnywhere annotation = method.getAnnotation(SyncToAnywhere.class);
        Class<? extends SyncToAnywhereHandler>[] handler = annotation.handler();
        return switch (annotation.type()) {
            // 增删改，都是要执行完成之后再进行操作，因为需要确保数据库的数据都操作完成了才进行同步
            case INSERT, DELETE, UPDATE, UNKNOWN -> {
                // 方法执行后的结果
                Object proceed = invocation.proceed();
                boolean result = false;
                if (proceed instanceof Boolean r) {
                    result = r;
                }
                if (proceed instanceof Integer r) {
                    result = r > 0;
                }
                if (result) {
                    //获取参数名对应数组下标
                    int index = ArrayUtil.indexOf(parameterNames, annotation.value());
                    if (index < 0) {
                        index = 0;
                    }
                    List<Object> argList = new ArrayList<>();
                    // 获取到操作对象
                    Object arg = args[index];
                    if (arg instanceof Collection c) {
                        // 如果是批量操作
                        argList.addAll(c);
                    } else {
                        // 不管是不是批量操作，都按批量的来
                        argList.add(arg);
                    }
                    SimpleDBOperateType type = annotation.type();
                    Class<?>[] convert = annotation.convert();
                    if (annotation.executeAsync()) {
                        // 直接异步执行
                        cachedThreadPool.execute(() -> {
                            execute(handler, convert, type, argList);
                        });
                    } else {
                        execute(handler, convert, type, argList);
                    }
                }
                yield proceed;
            }
            case TOTAL -> {
                try {
                    //获取参数名对应数组下标
                    int index = ArrayUtil.indexOf(parameterNames, annotation.value());
                    if (index < 0) {
                        index = 0;
                    }
                    // 获取查询参数
                    Object params = args[index];
                    // 获取查询分页参数
                    Class<?> queryDataClass = annotation.queryDataClass();
                    if (ObjectUtil.isNotEmpty(params) && ArrayUtil.isNotEmpty(handler) && !queryDataClass.equals(Void.class)) {
                        Class<? extends QueryConditionHandler> queryConditionHandlerClazz = annotation.queryConditionHandler();
                        // 先从 Spring boot 容器里面拿，没有再静态创建一个
                        QueryConditionHandler queryConditionHandler = null;
                        try {
                            queryConditionHandler = SpringUtil.getBean(queryConditionHandlerClazz);
                        } catch (NoSuchBeanDefinitionException e) {
                            log.trace("未找到注册的 bean!");
                        }
                        if (ArrayUtil.isEmpty(queryConditionHandler)) {
                            Constructor<? extends QueryConditionHandler> queryConditionHandlerDeclaredConstructor = queryConditionHandlerClazz.getDeclaredConstructor();
                            queryConditionHandler = queryConditionHandlerDeclaredConstructor.newInstance();
                        }
                        SyncToAnywhereHandler bean = SpringUtil.getBean(handler[0]);
                        if (params instanceof JSONObject p) {
                            yield bean.total(p, queryConditionHandler, queryDataClass);
                        }
                        if (params instanceof MybatisOptional<?> p) {
                            yield bean.total(p, queryConditionHandler, queryDataClass);
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                // 方法执行后的结果
                yield invocation.proceed();
            }
            case PAGE -> {
                try {
                    //获取参数名对应数组下标
                    int index = ArrayUtil.indexOf(parameterNames, annotation.value());
                    if (index < 0) {
                        index = 0;
                    }
                    // 获取查询参数
                    Object params = args[index];
                    // 获取查询分页参数
                    Class<?> queryDataClass = annotation.queryDataClass();
                    if (ObjectUtil.isNotEmpty(params) && ArrayUtil.isNotEmpty(handler) && !queryDataClass.equals(Void.class)) {
                        Class<? extends QueryConditionHandler> queryConditionHandlerClazz = annotation.queryConditionHandler();
                        // 先从 Spring boot 容器里面拿，没有再静态创建一个
                        QueryConditionHandler queryConditionHandler = null;
                        try {
                            queryConditionHandler = SpringUtil.getBean(queryConditionHandlerClazz);
                        } catch (NoSuchBeanDefinitionException e) {
                            log.trace("未找到注册的 bean!");
                        }
                        if (ArrayUtil.isEmpty(queryConditionHandler)) {
                            Constructor<? extends QueryConditionHandler> queryConditionHandlerDeclaredConstructor = queryConditionHandlerClazz.getDeclaredConstructor();
                            queryConditionHandler = queryConditionHandlerDeclaredConstructor.newInstance();
                        }
                        SyncToAnywhereHandler bean = SpringUtil.getBean(handler[0]);
                        IPage result = null;
                        if (params instanceof JSONObject p) {
                            Object page = null;
                            if (args.length > 1) {
                                index = ArrayUtil.indexOf(parameterNames, annotation.page());
                                if (index < 0) {
                                    index = 1;
                                }
                                page = args[index];
                            }
                            if (ObjectUtil.isNotEmpty(page) && page instanceof SqlPageParams s) {
                                result = bean.page(p, s, queryConditionHandler, queryDataClass);
                            } else {
                                result = bean.page(p, queryConditionHandler, queryDataClass);
                            }
                        }
                        if (params instanceof MybatisOptional<?> p) {
                            result = bean.page(p, queryConditionHandler, queryDataClass);
                        }
                        List records = result.getRecords();
                        if (ObjectUtil.isNotEmpty(records)) {
                            Class<?> queryResultConverter = annotation.queryResultConverter();
                            if (IPage.class.isAssignableFrom(returnType)) {
                                if (!queryResultConverter.equals(Void.class)) {
                                    List<?> copyToList = BeanUtil.copyToList(records, queryResultConverter);
                                    result.setRecords(copyToList);
                                }
                                yield result;
                            }
                            if (Collection.class.isAssignableFrom(returnType)) {
                                yield queryResultConverter.equals(Void.class) ? records : BeanUtil.copyToList(records, queryResultConverter);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                // 方法执行后的结果
                yield invocation.proceed();
            }
        };
    }

    /**
     * 执行操作
     *
     * @param handler 所有的处理器
     * @param convert 处理器需要的数据类型
     * @param type    操作类型
     * @param argList 参数列表
     */
    private static void execute(Class<? extends SyncToAnywhereHandler>[] handler, Class<?>[] convert, SimpleDBOperateType type, List<Object> argList) {
        if (ArrayUtil.isNotEmpty(handler)) {
            for (int i = 0; i < handler.length; i++) {
                SyncToAnywhereHandler syncToAnywhereHandler = SpringUtil.getBean(handler[i]);
                if (ArrayUtil.isNotEmpty(convert)) {
                    if (type.equals(SimpleDBOperateType.DELETE)) {
                        // 如果需要转换类型就按转换类型的来传递
                        execute(syncToAnywhereHandler, type, argList, convert[i]);
                    } else {
                        // 如果需要转换类型就按转换类型的来传递
                        execute(syncToAnywhereHandler, type, BeanUtil.copyToList(argList, convert[i]), convert[i]);
                    }
                } else {
                    execute(syncToAnywhereHandler, type, argList.stream().map(a -> (Serializable) a).collect(Collectors.toSet()), null);
                }
            }
        }
    }

    /**
     * 执行操作
     *
     * @param syncToAnywhereHandler 处理器
     * @param type                  类型
     * @param serializableSet       需要处理的数据
     */
    private static void execute(SyncToAnywhereHandler syncToAnywhereHandler
            , SimpleDBOperateType type
            , Collection<?> serializableSet
            , Class<?> convert) {
        if (type.equals(SimpleDBOperateType.INSERT)) {
            syncToAnywhereHandler.insert(serializableSet);
        }
        if (type.equals(SimpleDBOperateType.UPDATE)) {
            syncToAnywhereHandler.update(serializableSet);
        }
        if (type.equals(SimpleDBOperateType.DELETE)) {
            syncToAnywhereHandler.delete(serializableSet, convert);
        }
        if (type.equals(SimpleDBOperateType.UNKNOWN)) {
            syncToAnywhereHandler.unknown(serializableSet);
        }
    }

}
