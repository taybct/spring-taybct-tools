package io.github.taybct.tool.core.bean;

import com.alibaba.fastjson2.JSONObject;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户的信息，这里拿到的是登录的用户的信息，是从 jwt token 里面解析出来的用户信息，如果想再加其他信息<br>
 * 1、 在 io.github.taybct.api.system.dto.OAuth2UserDTO 里面加信息<br>
 * 2、 在 io.github.taybct.auth.security.pojo.OAuth2UserDetails 里面加信息<br>
 * 3、 在 io.github.taybct.auth.security.config.AuthorizationServerConfig.tokenEnhancer() 里面把信息写入到 jwt token 的载体里面<br>
 *
 * @author xijieyin <br> 2022/8/5 17:41
 * @since 1.0.0
 */
public interface ILoginUser extends Serializable {
    public Long getUserId();

    /**
     * 用户名
     */
    String getUsername();

    /**
     * 用户角色编码（权限）集合<br>
     * {@code ["ADMIN","NORMAL","LEADER"...]}
     */
    Set<String> getAuthorities();

    /**
     * 权限范围
     */
    String getScope();

    /**
     * 超时时间
     */
    Long getExp();

    /**
     * jwt token id
     */
    String getJti();

    /**
     * 客户端 id
     */
    String getClientId();

    /**
     * 租户 id
     */
    String getTenantId();

    /**
     * Token 数据参数载体
     */
    JSONObject getPayload();

    /**
     * 检查有没有 ROOT 权限（是否是 ROOT 用户）
     *
     * @return int
     * @author xijieyin
     */
    int checkRoot();

    /**
     * 检查有没有权限，返回权限列表
     *
     * @return {@code Set<String>}
     * @author xijieyin
     */
    Set<String> checkAuthorities();

    /**
     * 是否有 ROOT 角色
     *
     * @return int
     */
    int hasRootRole();

    /**
     * 是否有 ADMIN 角色
     *
     * @return int
     */
    int hasAdminRole();
}
