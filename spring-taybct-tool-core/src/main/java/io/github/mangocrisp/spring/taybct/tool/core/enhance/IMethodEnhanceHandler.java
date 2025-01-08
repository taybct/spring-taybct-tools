package io.github.mangocrisp.spring.taybct.tool.core.enhance;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 方法增强处理器
 *
 * @author XiJieYin <br> 2024/4/19 13:50
 */
public interface IMethodEnhanceHandler {

    /**
     * 方法执行之前操作
     *
     * @param method         执行的方法
     * @param argumentsNames 执行的方法的参数名
     * @param arguments      执行的方法的参数
     * @param aThis          当前对象
     * @param staticPart     The static part is an accessible object on which a chain of interceptors is installed
     */
    default void before(Method method
            , String[] argumentsNames
            , Object[] arguments
            , Object aThis
            , AccessibleObject staticPart) {
    }

    /**
     * 方法执行之后操作
     *
     * @param method         执行的方法
     * @param argumentsNames 执行的方法的参数名
     * @param arguments      执行的方法的参数
     * @param aThis          当前对象
     * @param staticPart     The static part is an accessible object on which a chain of interceptors is installed
     * @param proceed        返回的结果
     */
    default void after(Method method
            , String[] argumentsNames
            , Object[] arguments
            , Object aThis
            , AccessibleObject staticPart
            , AtomicReference<Object> proceed) {
    }

    /**
     * 方法执行报错之后操作
     *
     * @param method         执行的方法
     * @param argumentsNames 执行的方法的参数名
     * @param arguments      执行的方法的参数
     * @param aThis          当前对象
     * @param staticPart     The static part is an accessible object on which a chain of interceptors is installed
     * @param throwable      抛出异常
     */
    default void afterThrows(Method method
            , String[] argumentsNames
            , Object[] arguments
            , Object aThis
            , AccessibleObject staticPart
            , Throwable throwable) {
    }

}
