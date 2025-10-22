package io.github.taybct.tool.core.bean;

/**
 * 登录用户信息接口
 *
 * @author xijieyin <br> 2022/8/5 17:37
 * @since 1.0.0
 */
public interface ISecurityUtil {

    /**
     * 获取已经登录的用户信息
     *
     * @return LoginUser
     * @author xijieyin <br> 2022/8/5 17:38
     * @since 1.0.0
     */
    ILoginUser getLoginUser();

}
