package io.github.taybct.tool.security.util;

import com.alibaba.fastjson2.JSONObject;
import io.github.taybct.tool.core.bean.ILoginUser;
import io.github.taybct.tool.core.bean.ISecurityUtil;
import io.github.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.taybct.tool.core.constant.TokenConstants;
import io.github.taybct.tool.core.exception.def.BaseException;
import io.github.taybct.tool.core.result.ResultCode;
import io.github.taybct.tool.core.util.sm.SM4Coder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * 登录用户信息工具类
 *
 * @author xijieyin <br> 2022/8/5 22:45
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class SecurityUtil implements ISecurityUtil {

    private final ISysParamsObtainService sysParamsObtainService;

    @Override
    public ILoginUser getLoginUser() {
        return new LoginUser(getJwtPayload(), sysParamsObtainService);
    }

    @SneakyThrows
    public JSONObject getJwtPayload() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .map(requestAttributes -> (ServletRequestAttributes) requestAttributes)
                .map(requestAttributes -> requestAttributes.getRequest().getHeader(TokenConstants.JWT_PAYLOAD_KEY))
                .map(payload -> {
                    try {
                        return JSONObject.parseObject(SM4Coder.getSM4().decryptStr(payload, StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new BaseException(ResultCode.TOKEN_INVALID_OR_EXPIRED.getMessage(), HttpStatus.UNAUTHORIZED, ResultCode.TOKEN_INVALID_OR_EXPIRED.getCode()));
        // 这里在网关加密了，所以需要解密一下
    }

}
