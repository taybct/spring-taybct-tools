package io.github.taybct.tool.core.constant;

/**
 * 缓存常量
 *
 * @author xijieyin <br> 2022/8/5 18:16
 * @since 1.0.0
 */
public class CacheConstants {

    /**
     * 系统前缀
     */
    public static String getSystemPrefix(){
        String property = java.lang.System.getProperty(AppConstants.CACHE_PREFIX_CONFIG_KEY);
        if (property == null) {
            property = java.lang.System.getenv(AppConstants.CACHE_PREFIX_CONFIG_ENV_KEY);
        }
        if (property == null) {
            property = "tb:";
        }
        return property;
    };

    /**
     * 系统模块相关的缓存
     */
    public interface System {
        String PREFIX = getSystemPrefix() + "sys:";
        /**
         * 系统参数
         */
        String PARAMS = PREFIX + "params";
    }

    /**
     * 默认数据缓存
     */
    public interface Params {

        String PREFIX = System.PARAMS;
        /**
         * 默认 ROOT 用户的 id 是 1
         */
        String USER_ROOT_ID = "user_root_id";
        /**
         * 默认租户 id
         */
        String TENANT_ID = "tenant_id";

    }

    /**
     * 任务调度相关
     */
    public interface Scheduled {
        /**
         * 前缀
         */
        String PREFIX = getSystemPrefix() + "tsk:";
    }

}
