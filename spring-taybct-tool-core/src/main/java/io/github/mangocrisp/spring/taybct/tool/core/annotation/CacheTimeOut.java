package io.github.mangocrisp.spring.taybct.tool.core.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存数据，可以设置超时时间
 * <br>
 * 所有的缓存条件的判断顺序是
 * <br>
 * <pre>{@code 1. condition < 2. removeCondition < 3. updateCondition < 4. key < 5. unless}</pre>
 * 即：
 * <br> 1. 放入缓存的条件判断，就是如果已经符合了，才会进行缓存操作
 * <br> 2. 如果删除了，就不需要更新了
 * <br> 3. 获取了结果，才能对结果进行更新，这里如果需要更行更新才会去获取结果，就可以直接返回了
 * <br> 4. 这里如果缓存里面没有 key 才会去获取结果
 * <br> 5. 这里肯定是缓存里没有 key，然后并且已经获取到了结果，才能对结果进行判断，判断是否要存入缓存，在判断之前如果缓存里面有数据，就直接获取缓存里面的数据
 * <br> 所以，removeCondition 和其他的条件如果使用了，后面的条件就无效了
 *
 * @author XiJieYin <br> 2023/2/6 23:24
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited                          //允许子类继承
public @interface CacheTimeOut {

    /**
     * Names of the caches in which method invocation results are stored.
     *
     * @return 缓存名
     */
    String cacheName();

    /**
     * Spring Expression Language (SpEL) expression for computing the key dynamically.
     * <p>
     * (SpEL)
     * <br>
     * 这个 key 如果是或者是经过 SpEL表达式计算的结果是使用 ","隔开，表示同时操作多个 key
     *
     * @return SqEL 表达式获取 key 名
     */
    String key();

    /**
     * 超时时间 默认 60
     *
     * @return 超时时间
     */
    long timeout() default 60L;

    /**
     * 时间单位 默认秒
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Spring Expression Language (SpEL) expression used for making the method caching conditional.
     * Default is "", meaning the method result is always cached.
     * <p>
     * (SpEL)
     * <br>
     * 缓存条件，与 {@linkplain org.springframework.cache.annotation.Cacheable#condition @Cacheable} 里面的 condition 一样，用来判定是否决定要缓存数据
     * 这里要求表达式最终得到的结果要是一个 boolean 即 "true" 或者 "false"，不过不管怎么样，只要不是 "true" 或者 "" 就都不会缓存
     *
     * @return 缓存条件
     */
    String condition() default "";

    /**
     * Spring Expression Language (SpEL) expression used to veto method caching.
     * Unlike condition, this expression is evaluated after the method has been called and can therefore refer to the result.
     * Default is "", meaning that caching is never vetoed.
     * <p>
     * (SpEL)
     * <br>
     * 当 unless 指定的条件为 true ，方法的返回值就不会被缓存，这里可以使用 #result 来引用返回结果
     *
     * @return 对返回结果做处理的条件
     */
    String unless() default "";

    /**
     * (SpEL)
     * <br>
     * 删除 key 的条件，即当条件为 true 的时候，删除 key，并且不再执行缓存操作
     *
     * @return 删除 key 的条件
     */
    String removeCondition() default "";

    /**
     * (SpEL)
     * <br>
     * 更新 key 的条件，即当条件为 true 的时候更新 key，并且直接返回
     *
     * @return 更新 key 的条件
     */
    String updateCondition() default "";

    /**
     * 更新的对象的参数名，这里如果不指定，默认拿第一个参数
     *
     * @return 参数名
     */
    String updateObject() default "";

}
