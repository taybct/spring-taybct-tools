package io.github.taybct.tool.security.util;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.taybct.tool.core.bean.ILoginUser;
import io.github.taybct.tool.core.constant.CacheConstants;
import io.github.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.taybct.tool.core.constant.ROLE;
import io.github.taybct.tool.core.constant.TokenConstants;
import io.github.taybct.tool.core.exception.def.BaseException;
import io.github.taybct.tool.core.result.ResultCode;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.HashSet;
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
@AllArgsConstructor
public class LoginUser implements ILoginUser {

    private static final long serialVersionUID = 5227035886108931032L;
    /**
     * Token 数据参数载体
     */
    private final JSONObject payload;

    private final ISysParamsObtainService sysParamsObtainService;

    @Override
    public Long getUserId() {
        return this.payload.getLong(TokenConstants.USER_ID_KEY);
    }

    @Override
    public String getUsername() {
        return this.payload.getString(TokenConstants.USER_NAME_KEY);
    }

    @Override
    public Set<String> getAuthorities() {
        if (!this.payload.containsKey(TokenConstants.JWT_AUTHORITIES_KEY)) {
            return Collections.emptySet();
        }
        return new HashSet<>(this.payload.getJSONArray(TokenConstants.JWT_AUTHORITIES_KEY).toJavaList(String.class));
    }

    @Override
    public String getScope() {
        return this.payload.getString(TokenConstants.SCOPE_KEY);
    }

    @Override
    public Long getExp() {
        return this.payload.getLong(TokenConstants.JWT_EXP);
    }

    @Override
    public String getJti() {
        return this.payload.getString(TokenConstants.JWT_JTI);
    }

    @Override
    public String getClientId() {
        return this.payload.getString(TokenConstants.CLIENT_ID_KEY);
    }

    @Override
    public String getTenantId() {
        return this.payload.getString(TokenConstants.TENANT_ID_KEY);
    }

    @Override
    public JSONObject getPayload() {
        return this.payload;
    }


    @Override
    public int checkRoot() {
        return getUserId()
                .equals(Long.valueOf(sysParamsObtainService.get(CacheConstants.Params.USER_ROOT_ID))) ? 1 : 0;
    }

    @Override
    public Set<String> checkAuthorities() {
        Set<String> authorities = getAuthorities();
        if (CollectionUtil.isEmpty(authorities)) {
            throw new BaseException(ResultCode.ACCESS_UNAUTHORIZED.getMessage(), HttpStatus.FORBIDDEN, ResultCode.ACCESS_UNAUTHORIZED.getCode());
        }
        return authorities;
    }

    @Override
    public int hasRootRole() {
        return checkAuthorities().stream().anyMatch(r -> r.equals(ROLE.ROOT)) ? 1 : 0;
    }

    @Override
    public int hasAdminRole() {
        return checkAuthorities().stream().anyMatch(r -> r.equals(ROLE.ROOT) || r.equals(ROLE.ADMIN)) ? 1 : 0;
    }

}
