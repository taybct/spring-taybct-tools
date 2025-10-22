package io.github.taybct.tool.core.redis;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.taybct.tool.core.constant.CacheConstants;
import io.github.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.taybct.tool.core.constant.PropertyParams;
import io.github.taybct.tool.core.interceptor.CacheTimeOutMethodInterceptor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Optional;

/**
 * redis配置
 *
 * @author xijieyin
 */
@EnableConfigurationProperties(CacheProperties.class)
@AutoConfiguration
@EnableCaching
@ConditionalOnClass(RedisTemplate.class)
public class RedisConfiguration extends CachingConfigurerSupport {

    @Bean
    public <K, V> RedisTemplate<K, V> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<K, V> template = new RedisTemplate<>();
        //template.setEnableTransactionSupport(true);
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance
                , ObjectMapper.DefaultTyping.NON_FINAL
                , JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(mapper, Object.class);

        // key 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(stringRedisSerializer);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash value
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        //下面代码解决LocalDateTime序列化与反序列化不一致问题
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 解决jackson2无法反序列化LocalDateTime的问题
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        Jackson2JsonRedisSerializer<Object> j2jrs = new Jackson2JsonRedisSerializer<>(om, Object.class);
        // 序列化 value 时使用此序列化方法
        template.setValueSerializer(j2jrs);
        template.setHashValueSerializer(j2jrs);


        return template;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText() {
        return """
                local key = KEYS[1]
                local count = tonumber(ARGV[1])
                local time = tonumber(ARGV[2])
                local current = redis.call('get', key);
                if current and tonumber(current) > count then
                    return current;
                end
                current = redis.call('incr', key)
                if tonumber(current) == 1 then
                    redis.call('expire', key, time)
                end
                return current;""";
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(90));
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    public DefaultPointcutAdvisor cacheTimeOutPointcutAdvisor(RedisTemplate<String, Object> redisTemplate) {
        CacheTimeOutMethodInterceptor methodInterceptor = new CacheTimeOutMethodInterceptor(
                key -> Boolean.TRUE.equals(redisTemplate.hasKey(key))
                , key -> redisTemplate.opsForValue().get(key)
                , cacheElement -> redisTemplate.opsForValue().set(cacheElement.key(), cacheElement.value(), cacheElement.timeout(), cacheElement.unit())
                , key -> {
            if (CollectionUtil.isNotEmpty(key)) {
                redisTemplate.delete(key);
            }
        });
        // 匹配一个切点，这里使用注解
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(io.github.taybct.tool.core.annotation.CacheTimeOut)");
        // 增强
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        // 增强的方法
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }

    @Bean
    public ISysParamsObtainService sysParamsObtainService(RedisTemplate<String, Object> redisTemplate
            , PropertyParams propertyParams) {
        return key -> {
            Assert.hasLength(key, "params key must be not empty.");
            return Optional.ofNullable(redisTemplate.opsForValue().get(String.format("%s::%s", CacheConstants.Params.PREFIX, key)))
                    .orElseGet(() -> propertyParams.getMap().get(key));
        };
    }

}
