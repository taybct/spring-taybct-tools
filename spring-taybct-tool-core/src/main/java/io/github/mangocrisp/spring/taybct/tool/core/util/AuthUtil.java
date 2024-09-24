package io.github.mangocrisp.spring.taybct.tool.core.util;

import cn.hutool.core.util.StrUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.AuthHeaderConstants;
import javax.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

/**
 * 鉴权工具类
 *
 * @author XiJieYin <br> 2023/5/22 17:49
 */
public class AuthUtil {

    /**
     * 获取到授权类型
     */
    @SneakyThrows
    public static String getGrantType() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        return request.getParameter(AuthHeaderConstants.GRANT_TYPE_KEY);
    }

    /**
     * 获取登录认证的客户端ID
     * 参数可以放在请求头，如果放在请求头，需要使用 base 64 加密
     */
    @SneakyThrows
    public static String getOAuth2ClientId() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        // 从请求路径中获取
        String clientId = request.getParameter(AuthHeaderConstants.CLIENT_ID_KEY);
        if (StrUtil.isNotBlank(clientId)) {
            return clientId;
        }
        // 从请求头获取
        String basic = request.getHeader(AuthHeaderConstants.AUTHORIZATION_KEY);
        if (StrUtil.isNotBlank(basic) && basic.startsWith(AuthHeaderConstants.BASIC_PREFIX)) {
            basic = basic.replace(AuthHeaderConstants.BASIC_PREFIX, Strings.EMPTY);
            String basicPlainText = new String(Base64.getDecoder().decode(basic.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            clientId = basicPlainText.split(":")[0]; //client:secret
        }
        return clientId;
    }

}
