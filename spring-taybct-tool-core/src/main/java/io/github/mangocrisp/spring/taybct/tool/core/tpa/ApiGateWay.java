package io.github.mangocrisp.spring.taybct.tool.core.tpa;

import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.exception.def.BaseException;
import io.github.mangocrisp.spring.taybct.tool.core.util.ObjectUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 广州市指挥中心110警情信息（汇聚）
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class ApiGateWay<Config extends ApiConfig> {

    /**
     * 请求参数
     */
    final Config config;
    /**
     * 登录的方法
     */
    @Setter
    protected BiFunction<Config, Header[], String> loginFun = (c, h) -> {
        throw new BaseException("未提供登录方法");
    };
    /**
     * 生成 token 的方法
     */
    @Setter
    protected Function<JSONObject, ApiToken> genTokenFun = r -> {
        throw new BaseException("未提供token转换方法");
    };
    /**
     * 指定第三方接口的名称
     */
    @Setter
    protected String thirdPartName = "第三方接口";

    /**
     * 获取到缓存 token 的 key
     */
    @Setter
    protected Function<ApiConfig, String> tokenCacheKeyFn = ApiConfig::getTokenCacheKey;

    /**
     * 检查返回结果
     *
     * <br> resultJSON          返回结果的对象
     * <br> defaultErrorMessage 默认失败消息
     */
    @Setter
    protected BiConsumer<JSONObject, String> checkResult = (resultJSON, defaultErrorMessage) -> {
    };

    /**
     * 检查登录信息
     *
     * <br> apiToken 登录信息
     */
    @Setter
    protected BiFunction<Config, ConcurrentMap<String, JSONObject>, JSONObject> checkLoginInfo = (config, clientTokenInfoMap) -> {
        JSONObject tokenInfo = null;
        for (int i = 0; ; i++) {
            if (clientTokenInfoMap.containsKey(tokenCacheKeyFn.apply(config))) {
                tokenInfo = clientTokenInfoMap.get(tokenCacheKeyFn.apply(config));
            }
            if (ObjectUtil.isEmpty(tokenInfo)) {
                if (i >= 3) {
                    throw new BaseException(thirdPartName + "登录失败！[count > 3]");
                }
                login();
            } else {
                return tokenInfo;
            }
        }
    };

    /**
     * 拼接包含登录信息的请求头
     *
     * <br> apiToken 登录信息
     * <br> return 请求头
     */
    @Setter
    protected Function<ApiToken, Header[]> getRequestWithTokenHeaders = apiToken -> {
        BasicHeader authorization = new BasicHeader("Authorization", apiToken.getToken());
        return new Header[]{authorization};
    };

    /**
     * 缓存客户端登录信息
     */
    @Getter
    protected ConcurrentMap<String, JSONObject> clientTokenInfoMap = new ConcurrentHashMap<>();

    /**
     * 登录
     */
    public void login() {
        String resultJSONStr = loginFun.apply(config, null);
        JSONObject resultJSON = JSONObject.parseObject(resultJSONStr);
        // 如果有 code 而且获取到的数据是 0 说明是登录失败！
        checkResult.accept(resultJSON, "身份验证失败！");
        String tokenCacheKey = tokenCacheKeyFn.apply(config);
        clientTokenInfoMap.put(tokenCacheKey, resultJSON);
    }

    /**
     * 请求带上 token 信息
     *
     * @param tokenInfoFunction 传 token 信息给需要调用的方法，然后返回结果
     * @param <E>               返回的结果
     * @return 结果
     */
    public <E> E withTokenInfo(BiFunction<Config, Header[], E> tokenInfoFunction) {
        JSONObject tokenInfo = checkLoginInfo.apply(config, clientTokenInfoMap);
        ApiToken apiToken = genTokenFun.apply(tokenInfo);
        return tokenInfoFunction.apply(config, getRequestWithTokenHeaders.apply(apiToken));
    }

    /**
     * 清除 token 信息
     */
    protected void clearTokenInfo() {
        clientTokenInfoMap.remove(tokenCacheKeyFn.apply(config));
    }

    /**
     * 带统一处理返回结果的方法
     *
     * @param tokenInfoFunction 传 token 信息给需要调用的方法，然后返回结果
     * @return 请求的返回结果
     */
    public JSONObject withTokenInfoJSON(BiFunction<Config, Header[], String> tokenInfoFunction) {
        for (int i = 0; ; i++) {
            try {
                log.debug("进行第({})次请求", i + 1);
                String resultJSONStr = withTokenInfo(tokenInfoFunction);
                // 失败：{msg:,code,version}
                JSONObject resultJSON = JSONObject.parseObject(resultJSONStr);
                checkResult.accept(resultJSON, thirdPartName + "调用失败！");
                return resultJSON;
            } catch (Exception e) {
                // 操作失败，有可能是登录失败了，这里删除 token 信息再请求一次
                clearTokenInfo();
                log.error("接口请求失败！", e);
                if (i >= 2) {
                    log.error("请求超过3次，退出操作！");
                    // 失败超过 3 次就不再继续了
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
