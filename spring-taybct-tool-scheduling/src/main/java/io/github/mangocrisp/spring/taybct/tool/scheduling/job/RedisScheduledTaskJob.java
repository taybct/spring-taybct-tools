package io.github.mangocrisp.spring.taybct.tool.scheduling.job;

import cn.hutool.core.net.NetUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 基于  Redis 加锁实现分布式定时任务
 *
 * @author xijieyin <br> 2023/3/10 下午4:42
 */
@RequiredArgsConstructor
@Slf4j
public abstract class RedisScheduledTaskJob extends AbstractScheduledTaskJob {

    final RedisTemplate<String, String> redisTemplate;

    final Environment env;

    /**
     * 获取最小的缓存时间,这是个阈值,意思是比这个值还小就没必要再缓存了
     *
     * @return 默认是 5 秒
     */
    protected long getMinCacheTime() {
        return 5L;
    }

    @SneakyThrows
    @Override
    public void run() {
        String ip = NetUtil.getLocalhost().getHostAddress();
        String hostName = NetUtil.getLocalhost().getHostName();
        String port = env.getProperty("server.port", "8080");
        String taskKey = CacheConstants.Scheduled.PREFIX + key;
        String serve = String.format("%s[%s]:%s", ip, hostName, port);
        // 未来的秒数
        long l = Objects.requireNonNull(CronExpression.parse(cron).next(LocalDateTime.now()))
                .toEpochSecond(ZoneOffset.of("+8"));
        // 当前的
        long i = l - LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(taskKey, serve, i, TimeUnit.SECONDS))) {
            try {
                super.run();
            } finally {
                // 未来的秒数
                l = Objects.requireNonNull(CronExpression.parse(cron).next(LocalDateTime.now()))
                        .toEpochSecond(ZoneOffset.of("+8"));
                // 当前的
                i = l - LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
                // 这里留个阈值,剩余比这个值少也就没有设置的必要了
                if (i <= getMinCacheTime()) {
                    redisTemplate.delete(taskKey);
                } else {
                    if (i - getMinCacheTime() > 0) {
                        redisTemplate.opsForValue().set(taskKey
                                , serve
                                , i - getMinCacheTime(), TimeUnit.SECONDS);
                    }
                }
            }
        } else {
            String s = redisTemplate.opsForValue().get(taskKey);
            log.warn("当前任务[{}]已经在服务[{}]启动!", key, s);
            if (Optional.ofNullable(redisTemplate.getExpire(taskKey, TimeUnit.SECONDS)).orElse(0L) < 0L) {
                // 可能会生成一些 -1 的异常数据，这里如果发现了，可以及时删除
                redisTemplate.delete(taskKey);
            }
        }
    }
}
