package io.github.mangocrisp.spring.taybct.tool.core.interceptor;

import io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceMethod;
import io.github.mangocrisp.spring.taybct.tool.core.enhance.IMethodEnhanceHandler;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.SpringUtil;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 方法增强拦截器
 *
 * @author XiJieYin <br> 2024/4/19 14:35
 */
@RequiredArgsConstructor
@Slf4j
public class MethodEnhanceInterceptor implements MethodInterceptor {

    /**
     * 默认的处理器集合
     */
    @NotNull
    final List<IMethodEnhanceHandler> allEnhanceHandler;

    @Nullable
    @Override
    public Object invoke(@NotNull MethodInvocation invocation) throws Throwable {
        if (ObjectUtil.isEmpty(invocation.getArguments()) && invocation.getMethod().getReturnType().equals(Void.class)) {
            // 如果没有参数而且返回类型是空，那就没必要做记录了
            return invocation.proceed();
        }
        // 获取到方法
        Method method = invocation.getMethod();
        // 获取到所有的参数名
        String[] parameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);
        // 获取到所有的参数
        Object[] args = invocation.getArguments();
        Object aThis = invocation.getThis();
        AccessibleObject staticPart = invocation.getStaticPart();
        // 获取到注解信息
        EnhanceMethod annotation = method.getAnnotation(EnhanceMethod.class);
        // 注解上的拦截器
        Class<? extends IMethodEnhanceHandler>[] handler = annotation.handler();
        for (Class<? extends IMethodEnhanceHandler> aClass : handler) {
            IMethodEnhanceHandler bean = SpringUtil.getBean(aClass);
            allEnhanceHandler.add(bean);
        }
        // 执行前操作
        allEnhanceHandler.forEach(h -> h.before(method, parameterNames, args, aThis, staticPart));
        // 返回结果
        AtomicReference<Object> proceed = new AtomicReference<>();
        try {
            proceed.set(invocation.proceed());
        } catch (Throwable e) {
            log.trace("方法执行失败！", e);
            allEnhanceHandler.forEach(h -> h.afterThrows(method, parameterNames, args, aThis, staticPart, e));
            throw new RuntimeException(e);
        }
        // 执行之后的操作
        allEnhanceHandler.forEach(h -> h.after(method, parameterNames, args, aThis, staticPart, proceed));
        return proceed.get();
    }

}
