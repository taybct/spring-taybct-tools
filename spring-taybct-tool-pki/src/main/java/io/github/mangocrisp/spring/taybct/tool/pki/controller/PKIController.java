package io.github.mangocrisp.spring.taybct.tool.pki.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.result.R;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import io.github.mangocrisp.spring.taybct.tool.pki.prop.PKIProp;
import io.github.mangocrisp.spring.taybct.tool.pki.util.JitGatewayUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PKI 登录处理
 *
 * @author XiJieYin <br> 2023/8/3 16:10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("pki")
@EnableConfigurationProperties({PKIProp.class})
@ConditionalOnClass({StringRedisTemplate.class})
public class PKIController {

    final PKIProp pkiProp;

    final StringRedisTemplate redisTemplate;

    final Environment env;


    @RequestMapping(value = "jitGWRandom")
    public R<?> randomFrom(HttpServletRequest request, HttpServletResponse response) {
        if (!JitGatewayUtil.initConfigBean(pkiProp)) {
            // 如果没有 token 直接返回报错
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return R.fail("配置文件不正确");
        }
        // 实例化网关工具类
        JitGatewayUtil jitGatewayUtil = new JitGatewayUtil();
        JSONObject data = new JSONObject();
        String randNum = null;
        // 如果没开启二维码
        if (!pkiProp.isQrCodeAuth()) {
            log.debug("====生成原文开始====");
            log.debug("调用应用服务器生成原文");
            randNum = jitGatewayUtil.generateRandomNum();
            if (StringUtil.isBlank(randNum)) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return R.fail("生成原文为空");
            }
            log.debug("生成原文结束，成功生成原文：{}", randNum);
        }
        data.put(JitGatewayUtil.AuthConstant.KEY_ORIGINAL_DATA, randNum);
        data.put(JitGatewayUtil.ConfigConstant.KEY_QRCODE_AUTH, pkiProp.isQrCodeAuth());
        data.put(JitGatewayUtil.AuthConstant.KEY_ORIGINAL, randNum);
        return R.data(data);
    }

    @PostMapping(value = "jitGWAuth")
    public R<?> jitGWAuth(HttpServletRequest request, HttpServletResponse response, @RequestBody JSONObject data) {
        String[] activeProfiles = env.getActiveProfiles();
        // 这里测试开发环境的数据
        if (ArrayUtil.indexOf(activeProfiles, "dev") > -1) {
            String username = "某某";
            String idCard = "xxxx********xxxx";
            String phone = "xxx****xxxx";
            JSONObject result = new JSONObject();
            result.put("username", username);
            result.put("idCard", idCard);
            String code = UUID.fastUUID().toString(true);
            result.put("code", code);
            // 这里缓存一个身份证 code ，用于登录的时候做验证，这里只设置60秒的缓存，过期就没用了
            redisTemplate.opsForValue().set(pkiProp.getOauthCachePrefix() + "pki:" + idCard
                    , code
                    , 60
                    , TimeUnit.SECONDS);
            return R.data(result);
        }
        log.debug("====身份认证开始！====");
        // 实例化网关工具类
        JitGatewayUtil jitGatewayUtil = new JitGatewayUtil();
        // 设置认证方式、报文token、session中认证原文、客户端认证原文、认证数据包、远程地址
        jitGatewayUtil.jitGatewayUtilBean.setAuthMode(data
                .getString(JitGatewayUtil.AuthConstant.MSG_AUTH_MODE));
        jitGatewayUtil.jitGatewayUtilBean.setToken(data
                .getString(JitGatewayUtil.AuthConstant.MSG_TOKEN));
        jitGatewayUtil.jitGatewayUtilBean.setOriginal_data(data
                .getString(JitGatewayUtil.AuthConstant.KEY_ORIGINAL_DATA));
        jitGatewayUtil.jitGatewayUtilBean.setOriginal_jsp(data
                .getString(JitGatewayUtil.AuthConstant.KEY_ORIGINAL));
        jitGatewayUtil.jitGatewayUtilBean.setSigned_data(data
                .getString(JitGatewayUtil.AuthConstant.KEY_SIGNED_DATA));
        jitGatewayUtil.jitGatewayUtilBean.setRemoteAddr(request.getRemoteAddr());

        // 从cookie中取得二维码随机数
        AtomicReference<String> qrcode = new AtomicReference<>();
        Arrays.stream(request.getCookies())
                .forEach((cookie) -> {
                    if (JitGatewayUtil.AuthConstant.KEY_JIT_QRCODE.equalsIgnoreCase(cookie.getName())) {
                        qrcode.set(cookie.getValue());
                    }
                });
        // 设置二维码随机数
        jitGatewayUtil.jitGatewayUtilBean.setQrcode(qrcode.get());

        // 调用网关工具类方式进行身份认证
        jitGatewayUtil.auth();
//                    return jitGatewayUtil.authResult;
//                    return R.data(jitGatewayUtil.authResult);
//                    if (!jitGatewayUtil.authResult.isSuccess()) {
//                        log.debug("身份认证失败，失败原因：" + jitGatewayUtil.authResult.getErrDesc());
//                        return R.fail(jitGatewayUtil.authResult.getErrCode(), jitGatewayUtil.authResult.getErrDesc());
//                    }
//                    // 认证属性信息
        Map certAttributeNodeMap = jitGatewayUtil.authResult.getCertAttributeNodeMap();
//                    // UMS信息
//                    Map umsAttributeNodeMap = jitGatewayUtil.authResult.getUmsAttributeNodeMap();
//                    // PMS信息
//                    Map pmsAttributeNodeMap = jitGatewayUtil.authResult.getPmsAttributeNodeMap();
//                    // 自定义信息
//                    Map customAttributeNodeMap = jitGatewayUtil.authResult.getCustomAttributeNodeMap();
//                    // pki登录
        String str = getUserInfo(certAttributeNodeMap);
        if (str.length() > 0) {
            String[] re = str.split(",");// CN=XM SFZ
            String[] xm_sfz = re[0].substring(3).split(" ");
            String username = xm_sfz[0];
            String idCard = xm_sfz[1];
            JSONObject result = new JSONObject();
            result.put("username", username);
            result.put("idCard", idCard);
            String code = UUID.fastUUID().toString(true);
            result.put("code", code);
            // 这里缓存一个身份证 code ，用于登录的时候做验证，这里只设置60秒的缓存，过期就没用了
            redisTemplate.opsForValue().set(pkiProp.getOauthCachePrefix() + "pki:" + idCard
                    , code
                    , 60
                    , TimeUnit.SECONDS);
            return R.data(result);
        }
        return R.data(jitGatewayUtil.authResult);
    }

    public String getUserInfo(Map umsAttributeNodeMap) {
        String str1 = "";
        String str0 = "";
        String strs = "";
        if (umsAttributeNodeMap != null && umsAttributeNodeMap.size() > 0) {
            Iterator umsIter = umsAttributeNodeMap.entrySet().iterator();
            while (umsIter.hasNext()) {
                Map.Entry entry = (Map.Entry) umsIter.next();
                Object key = entry.getKey();
                String[] keys = null;
                if (key != null && !key.equals("")) {
                    keys = (String[]) key;
                    Object val = entry.getValue();
                    if (val != null) {
                        if (keys[1] != null) {
                            strs = keys[1];
                        } else {
                            str0 = keys[0];
                        }
                        str1 = val.toString();
                    }
                }
            }
            System.out.println(str1);
        } else {

        }
        return str1;
    }

}
