package io.github.mangocrisp.spring.taybct.tool.wechat.config;

import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.util.HttpClientUtil;
import io.github.mangocrisp.spring.taybct.tool.wechat.constants.CacheConstants;
import io.github.mangocrisp.spring.taybct.tool.wechat.util.Decript;
import javax.annotation.Resource;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 微信操作参数配置
 *
 * @author xijieyin <br> 2022/8/5 22:57
 * @since 1.0.0
 */
@Data
@AutoConfiguration
@ConfigurationProperties("taybct.wechat")
@Slf4j
public class WechatConfig {
    /**
     * 微信开放平台 appid
     * 应用唯一标识，在微信开放平台提交应用审核通过后获得
     */
    private String appId;
    /**
     * 应用密钥AppSecret，在微信开放平台提交应用审核通过后获得
     */
    private String appSecret;
    /**
     * 回调地址，用于获取授权 code
     * 请使用 urlEncode 对链接进行处理
     */
    private String redirectUrl;
    /**
     * 自己平台应用的首页，或者说是登录成功后的页面
     */
    private String loginSuccessPage = "";
    /**
     * 用于保持请求和回调的状态，授权请求后原样带回给第三方。
     * 该参数可用于防止 csrf 攻击（跨站请求伪造攻击），
     * 建议第三方带上该参数，可设置为简单的随机数加 session 进行校验。
     * 在 state 传递的过程中会将该参数作为 url 的一部分进行处理，
     * 因此建议对该参数进行url encode操作，防止其中含有影响 url 解析的特殊字符（如'#'、'&'等）导致该参数无法正确回传。
     */
    private String state = "taybct";
    /**
     * state 超时，redis 存储时间，如果超过 多少分钟不操作，就认为是超时了，单位是 秒
     */
    private Long stateTimeOut = 300L;
    /**
     * 用于验证消息的确来自微信服务器
     */
    private String signatureToken = "wxtoken";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * <a href="https://developers.weixin.qq.com/doc/oplatform/Website_App/WeChat_Login/Wechat_Login.html">微信开放文档<a/>
     * <p>
     * 微信开放平台授权baseUrl  %s相当于?代表占位符
     * scope: 应用授权作用域，拥有多个作用域用逗号（,）分隔，网页应用目前仅填写snsapi_login
     */
    final static String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
            "?appid=%s" +
            "&redirect_uri=%s" +
            "&response_type=code" +
            "&scope=snsapi_login" +
            "&state=%s" +
            "#wechat_redirect";

    private static final String SCOPE = "snsapi_userinfo";//  snsapi_userinfo  snsapi_base

    /**
     * 调用微信的二维码需要一系列的认证，这里可以直接调用微信的登录接口
     */
    final static String wxAuthorizationUrl = "https://open.weixin.qq.com/connect/oauth2/authorize" +
            "?appid=%s" +
            "&redirect_uri=%s" +
            "&response_type=code" +
            "&scope=%s" +
            "&state=%s" +
            "#wechat_redirect";

    public String getState() {

        return this.state;
    }

    @SneakyThrows
    public String getAuthorizationUrl(String state) {
        redisTemplate.opsForValue().set(CacheConstants.WX.STATE + state, state, this.stateTimeOut, TimeUnit.SECONDS);
        return String.format(wxAuthorizationUrl,
                this.appId
                , URLEncoder.encode(this.redirectUrl, StandardCharsets.UTF_8.name())
                , SCOPE
                , state
        );
    }

    /**
     * 第一步：请求CODE
     */
    @SneakyThrows
    public String getWxCodeUrl(String state) {
        redisTemplate.opsForValue().set(CacheConstants.WX.STATE + state, state, this.stateTimeOut, TimeUnit.SECONDS);
        return String.format(baseUrl,
                this.appId
                , URLEncoder.encode(this.redirectUrl, StandardCharsets.UTF_8.name())
                , state
        );
    }

    final static String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
            "?appid=%s" +
            "&secret=%s" +
            "&code=%s" +
            "&grant_type=authorization_code";

    /**
     * 第二步：通过 code 获取access_token
     *
     * @param code  微信扫码后返回的 code 填写第一步获取的 code 参数
     * @param state 微信扫码之前平台传给微信的 code，微信应该要原样返回，如果返回的 state 和之前传的不一致，就报错
     */
    public String getWxAccessTokenUrl(String code, String state) {
        Assert.isTrue(Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstants.WX.STATE + state)), "返回 state 不正确！");
        redisTemplate.delete(CacheConstants.WX.STATE + state);
        return String.format(baseAccessTokenUrl,
                this.appId
                , this.appSecret
                , code
        );
    }

    /**
     * 获取 access token
     * 正确的返回：
     * {
     * "access_token":"ACCESS_TOKEN",
     * "expires_in":7200,
     * "refresh_token":"REFRESH_TOKEN",
     * "openid":"OPENID",
     * "scope":"SCOPE",
     * "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     */
    public JSONObject getWxAccessToken(String wxAccessTokenUrl, String state) {
        String result = HttpClientUtil.doGet(wxAccessTokenUrl);
        JSONObject accessTokenInfo = JSONObject.parseObject(result);
        redisTemplate.opsForValue().set(CacheConstants.WX.TOKEN_INFO + state, accessTokenInfo, stateTimeOut, TimeUnit.SECONDS);
        return accessTokenInfo;
    }

    /**
     * 刷新 token url
     */
    final static String baseRefreshTokenUrl = "https://api.weixin.qq.com/sns/oauth2/refresh_token" +
            "?appid=%s" +
            "&grant_type=refresh_token" +
            "&refresh_token=%s";

    /**
     * 获取到刷新 token 的 url
     *
     * @param refresh_token 之前获取 access token 的时候返回的 refresh_token
     */
    public String getBaseRefreshTokenUrl(String refresh_token) {
        return String.format(baseRefreshTokenUrl,
                this.appId,
                refresh_token
        );
    }

    /**
     * 刷新 token 获取到新的 access_token
     *
     * @param refreshTokenUrl 刷新 token 的地址
     * @param state           储存 redis 的唯一标识
     */
    public JSONObject refreshToken(String refreshTokenUrl, String state) {
        String result = HttpClientUtil.doGet(refreshTokenUrl);
        JSONObject accessTokenInfo = JSONObject.parseObject(result);
        redisTemplate.opsForValue().set(CacheConstants.WX.TOKEN_INFO + state, accessTokenInfo, stateTimeOut, TimeUnit.SECONDS);
        return accessTokenInfo;
    }

    /**
     * 获取用户个人信息
     */
    final static String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
            "?access_token=%s" +
            "&openid=%s";

    /**
     * 第三步：获取用户信息
     *
     * @param accessToken 第二步获取到的 access_token
     * @param openid      同上
     */
    public String getUserInfoUrl(String accessToken, String openid) {
        return String.format(
                baseUserInfoUrl,
                accessToken,
                openid
        );
    }

    /**
     * 获取用户信息
     * 正确的返回：
     * {
     * "country": "",
     * "province": "",
     * "city": "",
     * "openid": "",
     * "sex": 0,
     * "nickname": "ㅤ",
     * "headimgurl": "头像",
     * "language": "",
     * "privilege": [
     * <p>
     * ]
     * }
     *
     * @param userInfoUrl 获取用户信息的 uri
     * @param state       存储 redis 唯一标识
     */
    public JSONObject getUserInfo(String userInfoUrl, String state) {
        String result = HttpClientUtil.doGet(userInfoUrl);
        JSONObject userInfo = JSONObject.parseObject(result);
        redisTemplate.opsForValue().set(CacheConstants.WX.USER_INFO + state, userInfo, stateTimeOut, TimeUnit.SECONDS);
        return userInfo;
    }


    /**
     * 验证消息的确来自微信服务器
     *
     * @param signature 签名，也就是加密后的我们的 signatureToken
     * @param timestamp 时间戳
     * @param nonce     随机数
     * @param echostr   随机字符串
     */
    public String signatureCheck(String signature, String timestamp, String nonce, String echostr) {
        //排序
        String sortString = sort(signatureToken, timestamp, nonce);
        //加密
        String mytoken = Decript.SHA1(sortString);
        //校验签名
        if (!mytoken.equals("") && mytoken.equals(signature)) {
            log.debug("签名校验通过。");
            return echostr; //如果检验成功输出echostr，微信服务器接收到此输出，才会确认检验完成。
        } else {
            log.debug("签名校验失败。");
            return null;
        }
    }

    /**
     * 排序方法
     */
    public static String sort(String token, String timestamp, String nonce) {
        String[] strArray = {token, timestamp, nonce};
        Arrays.sort(strArray);

        StringBuilder sbuilder = new StringBuilder();
        for (String str : strArray) {
            sbuilder.append(str);
        }

        return sbuilder.toString();
    }
}
