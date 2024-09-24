package io.github.mangocrisp.spring.taybct.tool.security.util;

import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ILoginUser;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.mangocrisp.spring.taybct.tool.core.constant.TokenConstants;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.result.ResultCode;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.sm.SM4Coder;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Dubbo 兼容的 Security 工具类，只有需要用到 dubbo 传输的时候可能会用到
 * <br>
 * 原因就是 dubbo 不是 http 协议传输数据，自然也不会有什么 HttpServletRequest 只能是通过 Dubbo
 * 的 <a href="https://cn.dubbo.apache.org/zh/docs3-v2/java-sdk/advanced-features-and-usage/service/attachment/">调用链路传递隐式参数</a>
 * 来传输数据，然后这里，我用到两个 Filter，一个传一个接，然后可以通过 {@code RpcContext.getContext()} 来获取上下文，然后获取参数
 * {@code @see io.github.mangocrisp.spring.taybct.tool.cloud.dubbo.filter.DubboConsumerContextFilter}
 * {@code }@see io.github.mangocrisp.spring.taybct.tool.cloud.dubbo.filter.DubboProviderContextFilter}
 *
 * @author XiJieYin <br> 2023/2/3 2:25
 */
@ConditionalOnClass({DubboService.class, RpcContext.class})
@RequiredArgsConstructor
public class DubboSecurityUtil implements ISecurityUtil {

    private final ISysParamsObtainService sysParamsObtainService;

    @Override
    public ILoginUser getLoginUser() {
        return new LoginUser(getJwtPayload(), sysParamsObtainService);
    }

    @SneakyThrows
    public JSONObject getJwtPayload() {
        HttpServletRequest request = ServletUtil.getRequest();
        String payload;
        if (request != null) {
            payload = request.getHeader(TokenConstants.JWT_PAYLOAD_KEY);
        } else {
            Map<String, String> attachments = RpcContext.getContext().getAttachments();
            payload = attachments.get(TokenConstants.JWT_PAYLOAD_KEY);
        }
        if (StringUtil.isBlank(payload)) {
            throw new BaseException(ResultCode.TOKEN_INVALID_OR_EXPIRED.getMessage(), HttpStatus.UNAUTHORIZED, ResultCode.TOKEN_INVALID_OR_EXPIRED.getCode());
        }
        return JSONObject.parseObject(SM4Coder.getSM4().decryptStr(payload, StandardCharsets.UTF_8));
    }

}
