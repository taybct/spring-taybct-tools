package io.github.mangocrisp.spring.taybct.tool.core.sms.constants;

/**
 * 缓存常量
 *
 * @author xijieyin
 */
public class CacheConstants {

    /**
     * 系统前缀
     */
    public static final String SYSTEM_PREFIX = "taybct:";

    /**
     * 存储短信验证码
     */
    public interface SMS {
        String PREFIX = SYSTEM_PREFIX + "sms:";
        String VERIFY = PREFIX + "verify:";
    }

}
