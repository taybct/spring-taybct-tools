package io.github.mangocrisp.spring.taybct.tool.wechat.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.wechat.config.WechatConfig;
import io.github.mangocrisp.spring.taybct.tool.wechat.constants.CacheConstants;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 微信配置请求控制器<br>
 * 这个类里面只是写了一些默认的请求，要使用这些请求，得有一个 controller 类来继承这个类
 * 然后实现这些请求
 *
 * @author xijieyin <br> 2022/8/5 22:58
 * @see org.springframework.stereotype.Controller
 * @see RestController
 * @since 1.0.0
 */
public class WechatController {

    @Autowired
    @Getter
    @Setter
    public WechatConfig wechatConfig;

    /**
     * 验证消息的确来自微信服务器
     *
     * @param signature 签名，也就是加密后的我们的 signatureToken
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     */
    @RequestMapping("/signatureCheck")
    @ResponseBody
    public String wxSignatureCheck(
            @RequestParam(value = "signature") String signature,
            @RequestParam(value = "timestamp") String timestamp,
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "echostr") String echostr
    ) {
        return wechatConfig.signatureCheck(signature, timestamp, nonce, echostr);
    }

    /**
     * 获取访问微信二维码
     *
     * @return 返回调用微信二维码的链接地址，前端负责调用调用一下
     */
    @GetMapping("qrCode")
    @ResponseBody
    public String wxQrCode() {
        return wechatConfig.getWxCodeUrl(UUID.fastUUID().toString(true));
    }

    /**
     * 微信授权地址
     */
    @GetMapping("authorization")
    @ResponseBody
    public JSONObject getAuthorizationUrl() {
        JSONObject result = new JSONObject();
        // 这个 state 到时候，用来确定是谁操作了这个登录操作，相当于是指定一个唯一用户
        String state = UUID.fastUUID().toString(true);
        String authorizationUrl = wechatConfig.getAuthorizationUrl(state);
        result.put("uri", authorizationUrl);
        result.put("state", state);
        return result;
    }

    /**
     * 微信端扫描二维码之后
     * 用户允许授权后，将会重定向到redirect_uri的网址上，并且带上 code 和state参数
     * {@literal redirect_uri?code=CODE&state=STATE}
     */
    @GetMapping("token")
    public String wxToken(@RequestParam String code, @RequestParam String state, Model model) {
        // 获取 access_token
        JSONObject wxAccessToken = wechatConfig.getWxAccessToken(wechatConfig.getWxAccessTokenUrl(code, state), state);
        // 获取完 access_token 之后，顺便获取用户信息
        wxUserInfo(wxAccessToken.getString("access_token"), wxAccessToken.getString("openid"), state);
        // 把需要跳转的页面放进去参数里面，默认的成功页面会判断，跳转
        model.addAttribute("loginSuccessPage", wechatConfig.getLoginSuccessPage());
        return "wx-login-success";
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 轮询检查登录状态
     *
     * @param state 传入之前的生成的登录唯一标识
     */
    @GetMapping("polling/{state}")
    @ResponseBody
    public JSONObject wxToken(@PathVariable String state) {
        JSONObject result = new JSONObject();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstants.WX.TOKEN_INFO + state))) {
            result.put("code", "200");
            result.put("data", redisTemplate.opsForValue().get(CacheConstants.WX.TOKEN_INFO + state));
            result.put("message", "登录成功！");
        } else {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstants.WX.STATE + state))) {
                result.put("code", "W401");
                result.put("message", "未登录！");
            } else {
                result.put("code", "W408");
                result.put("message", "登录超时，请刷新二维码！");
            }
        }
        return result;
    }

    /**
     * 获取微信用户信息
     */
    @GetMapping("userInfo")
    @ResponseBody
    public JSONObject wxUserInfo(@RequestParam String accessToken, @RequestParam String openid, @RequestParam String state) {
        return wechatConfig.getUserInfo(wechatConfig.getUserInfoUrl(accessToken, openid), state);
    }

}
