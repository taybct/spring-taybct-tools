package io.github.mangocrisp.spring.taybct.tool.core.interceptor;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.CacheTimeOut;
import io.github.mangocrisp.spring.taybct.tool.core.bean.CacheElement;
import io.github.mangocrisp.spring.taybct.tool.core.util.AOPUtil;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author XiJieYin <br> 2023/2/7 22:58
 */
@RequiredArgsConstructor
public class CacheTimeOutMethodInterceptor implements MethodInterceptor {

    /**
     * 检查是否有缓存键
     */
    final Predicate<String> hasKey;
    /**
     * 根据缓存键获取缓存值
     */
    final Function<String, Object> getValue;
    /**
     * 保存值
     */
    final Consumer<CacheElement> saveValue;
    /**
     * 删除缓存键
     */
    final Consumer<Collection<String>> removeKey;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (ObjectUtil.isNull(methodInvocation.getArguments())
                || methodInvocation.getMethod().getReturnType().equals(Void.class)) {
            // 如果没有参数，或者是返回类型是空，那就没必要做存储了
            return methodInvocation.proceed();
        }
        Method method = methodInvocation.getMethod();
        CacheTimeOut cacheTimeOut = method.getAnnotation(CacheTimeOut.class);
        String key = AOPUtil.generateKeyBySpEL(cacheTimeOut.key(), methodInvocation);
        if (StrUtil.isBlank(key)) {
            // 没指定 key 也返回了直接
            return methodInvocation.proceed();
        }
        // 判断是否条件进行缓存操作的条件
        String condition = AOPUtil.generateKeyBySpEL(cacheTimeOut.condition(), methodInvocation);
        if (!condition.equals("true") && !condition.equals("")) {
            return methodInvocation.proceed();
        }
        // key set
        Set<String> cacheKeySet = Arrays.stream(key.split(","))
                .map(k -> String.format("%s::%s", cacheTimeOut.cacheName(), k))
                .collect(Collectors.toSet());
        // 这里如果是删除条件就直接删除缓存 key，然后返回结果
        String removeCondition = AOPUtil.generateKeyBySpEL(cacheTimeOut.removeCondition(), methodInvocation);
        if (removeCondition.equals("true")) {
            Object result = methodInvocation.proceed();
            // 然后判断结果是否条件进行缓存的条件
            String unless = AOPUtil.generateKeyBySpEL(cacheTimeOut.unless(), methodInvocation, () -> result);
            if (unless.equals("true")) {
                return result;
            }
            // 如果达到了删除 key 的条件就直接删除 key，然后原样返回
            removeKey.accept(cacheKeySet);
            return result;
        }
        long timeout = cacheTimeOut.timeout();
        TimeUnit timeUnit = cacheTimeOut.timeUnit();
        // 判断是否进行更新操作
        String updateCondition = AOPUtil.generateKeyBySpEL(cacheTimeOut.updateCondition(), methodInvocation);
        if (updateCondition.equals("true")) {
            // 更新操作也会有返回结果，这里同样对反思结果进行判断
            int objectIndex = ArrayUtil.indexOf(new DefaultParameterNameDiscoverer().getParameterNames(method)
                    , cacheTimeOut.updateObject());
            if (objectIndex == -1) {
                // 如果找不到对象，这里默认拿第一个参数
                objectIndex = 0;
            }
            Object result = methodInvocation.proceed();
            // 然后判断结果是否条件进行缓存的条件
            String unless = AOPUtil.generateKeyBySpEL(cacheTimeOut.unless(), methodInvocation, () -> result);
            if (unless.equals("true")) {
                return result;
            }
            for (String k : cacheKeySet) {
                saveValue.accept(new CacheElement(k
                        , methodInvocation.getArguments()[objectIndex]
                        , timeout
                        , timeUnit));
            }
            return result;
        }
        for (String k : cacheKeySet) {
            // 判断如果有缓存，就返回缓存里面的结果
            if (Boolean.TRUE.equals(hasKey.test(k))) {
                Object o = getValue.apply(k);
                if (o != null) {
                    // 这里直接比较类，对不上就不返回
                    if (method.getReturnType().isAssignableFrom(o.getClass())) {
                        return o;
                    }
                }
            }
        }
        // 如果缓存里面没有结果，就去获取结果
        Object result = methodInvocation.proceed();
        // 然后判断结果是否条件进行缓存的条件
        String unless = AOPUtil.generateKeyBySpEL(cacheTimeOut.unless(), methodInvocation, () -> result);
        if (unless.equals("true")) {
            return result;
        }
        // 返回结果不为 null 才进行缓存
        if (result != null) {
            for (String k : cacheKeySet) {
                saveValue.accept(new CacheElement(k, result, timeout, timeUnit));
            }
        }
        return result;
    }

}