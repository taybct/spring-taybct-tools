package io.github.mangocrisp.spring.taybct.tool.wechat.constants;

/**
 * 缓存常量
 *
 * @author xijieyin <br> 2022/8/5 22:58
 * @since 1.0.0
 */
public class CacheConstants {

    /**
     * 系统前缀
     */
    public static final String SYSTEM_PREFIX = "taybct:";

    /**
     * 微信相关
     */
    public interface WX {

        String PREFIX = SYSTEM_PREFIX + "wx:";
        /**
         * 向微信发送 Authorization 请求时会传一个 state ，这个 state 先存储在 redis 里面，
         * 微信授权通过会原样把这个 state 返回，这里，可以做一个验证，确定是微信给我们发的 state
         */
        String STATE = PREFIX + "state:";
        /**
         * 存储微信登录成功后的 token 信息
         */
        String TOKEN_INFO = PREFIX + "token_info:";
        /**
         * 存储微信登录成功后的用户信息
         */
        String USER_INFO = PREFIX + "user_info:";

    }

}
