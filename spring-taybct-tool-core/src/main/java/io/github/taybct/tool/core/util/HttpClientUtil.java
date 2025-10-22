package io.github.taybct.tool.core.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class HttpClientUtil {

    private static final String FORMAT_HEADER = "-H '%1$s:%2$s'";
    private static final String FORMAT_METHOD = "%1$s";
    private static final String FORMAT_BODY = "-d '%1$s'";
    private static final String FORMAT_FORM_URLENCODED = "--data-urlencode '%1$s'";
    private static final String FORMAT_URL = "--location '%1$s'";
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * <p>
     * HttpServletRequest 转化为 CURL 命令
     * </p>
     *
     * @param request request http 请求
     * @return String
     */
    public static String getCurl(HttpUriRequest request) {
        String curl;
        try {
            List<String> parts = new ArrayList<>();
            parts.add("curl --location --request");
            String url = request.getUri().toString();
            String method = request.getMethod();
            String contentType = request.getEntity().getContentType();
            String queryString = request.getUri().getQuery();
            parts.add(String.format(FORMAT_METHOD, method.toUpperCase()));

            if (StrUtil.isNotEmpty(queryString)) {
                url = HttpUtil.urlWithForm(url, queryString, CharsetUtil.CHARSET_UTF_8, false);
            }
            parts.add(String.format(FORMAT_URL, url));

            Map<String, String> headers = new HashMap<>(16);
            for (Header header : request.getHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            headers.forEach((k, v) -> parts.add(String.format(FORMAT_HEADER, k, v)));
            if (StrUtil.isNotEmpty(contentType) && !headers.containsKey(CONTENT_TYPE)) {
                if (cn.hutool.http.ContentType.isFormUrlEncode(contentType)) {
                    parts.add(String.format(FORMAT_HEADER, CONTENT_TYPE, cn.hutool.http.ContentType.FORM_URLENCODED));
                } else {
                    parts.add(String.format(FORMAT_HEADER, CONTENT_TYPE, contentType));
                }
            }

            if (cn.hutool.http.ContentType.isFormUrlEncode(contentType)
                    && request.getEntity() instanceof UrlEncodedFormEntity entity) {
                String content = IoUtil.readUtf8(entity.getContent());
                if (StrUtil.isNotEmpty(content)) {
                    for (String s : content.split("&")) {
                        parts.add(String.format(FORMAT_FORM_URLENCODED, s));
                    }
                }
            }
            if (StrUtil.startWithIgnoreCase(contentType, cn.hutool.http.ContentType.JSON.toString())
                    && request.getEntity() instanceof StringEntity entity) {
                String content = IoUtil.readUtf8(entity.getContent());
                if (StrUtil.isNotEmpty(content)) {
                    parts.add(String.format(FORMAT_BODY, content));
                }
            }
            curl = StrUtil.join(" ", parts);
        } catch (Exception e) {
            log.debug("生成 curl 失败！", e);
            curl = null;
        }
        return curl;
    }

    /**
     * 转换 Map 对象成 Header 对象
     *
     * @param map 键值对
     * @return 转后的
     */
    public static Header[] convertMap2Headers(Map<String, String> map) {
        List<Header> headers = new ArrayList<>();
        map.forEach((k, v) -> headers.add(new BasicHeader(k, v)));
        return ArrayUtil.toArray(headers, Header.class);
    }

    /**
     * 发送 get 请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param param   请求参数
     * @return String
     * @author xijieyin <br> 2022/8/5 19:37
     * @since 1.0.0
     */
    public static String doGet(String url
            , Header[] headers
            , Map<String, String> param) {
        return doGet(url, headers, param, closeableHttpResponse -> {
        });
    }

    /**
     * 发送 get 请求
     *
     * @param url              请求地址
     * @param headers          请求头
     * @param param            请求参数
     * @param responseConsumer 消费 response
     * @return String
     */
    public static String doGet(String url
            , Header[] headers
            , Map<String, String> param
            , Consumer<CloseableHttpResponse> responseConsumer) {
        return doRequest(Optional.ofNullable(param).map(m -> new ConcurrentHashMap<String, Object>(param)).orElse(null)
                , () -> {
                    URI uri;
                    try {
                        uri = makeUrlParameter(url, param);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    // 创建http GET请求
                    HttpGet httpGet = new HttpGet(uri);
                    // 设置请求头
                    httpGet.setHeaders(headers);
                    httpGet.setConfig(RequestConfig.custom().build());
                    return httpGet;
                }, responseConsumer);
    }

    /**
     * 拼接 rul 参数
     *
     * @param url   url
     * @param param 请求参数
     * @return 拼接后的 url
     * @throws URISyntaxException 拼接报错
     */
    public static URI makeUrlParameter(String url, Map<String, String> param) throws URISyntaxException {
        // 创建uri
        URIBuilder builder = new URIBuilder(url);
        if (param != null) {
            param.forEach(builder::addParameter);
        }
        return builder.build();
    }

    /**
     * 发送 get 请求
     *
     * @param url 请求 url
     * @return String
     * @author xijieyin <br> 2022/8/5 19:38
     * @since 1.0.0
     */
    public static String doGet(String url) {
        return doGet(url, null, null);
    }

    /**
     * 发送 post 请求（表单请求）
     *
     * @param url     请求 url
     * @param headers 请求头
     * @param param   请求参数
     * @return String
     * @author xijieyin <br> 2022/8/5 19:38
     * @since 1.0.0
     */
    public static String doPost(String url
            , Header[] headers
            , Map<String, Object> param) {
        return doPost(url, headers, param, closeableHttpResponse -> {
        });
    }

    /**
     * 发送 post 请求（表单请求）
     *
     * @param url              请求 url
     * @param headers          请求头
     * @param param            请求参数
     * @param responseConsumer 消费 response
     * @return String
     */
    public static String doPost(String url
            , Header[] headers
            , Map<String, Object> param
            , Consumer<CloseableHttpResponse> responseConsumer) {
        return doRequest(param
                , () -> {
                    // 创建Http Post请求
                    HttpPost httpPost = new HttpPost(url);
                    // 设置请求头
                    httpPost.setHeaders(headers);
                    // 创建参数列表
                    if (param != null) {
                        List<NameValuePair> paramList = new ArrayList<>();
                        param.forEach((key, value) -> paramList.add(new BasicNameValuePair(key, (String) value)));
                        // 模拟表单
                        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
                        httpPost.setEntity(entity);
                    }
                    httpPost.setConfig(RequestConfig.custom().build());
                    return httpPost;
                }, responseConsumer);
    }

    /**
     * 发送请求
     *
     * @param param                  请求参数
     * @param httpUriRequestSupplier 提供一个请求
     * @return String
     */
    public static String doRequest(Map<String, Object> param
            , Supplier<HttpUriRequest> httpUriRequestSupplier) {
        return doRequest(param, httpUriRequestSupplier, closeableHttpResponse -> {
        });
    }

    /**
     * 发送请求
     *
     * @param param                  请求参数
     * @param httpUriRequestSupplier 提供一个请求
     * @param responseConsumer       消费 response
     * @return String
     */
    public static String doRequest(Map<String, Object> param
            , Supplier<HttpUriRequest> httpUriRequestSupplier
            , Consumer<CloseableHttpResponse> responseConsumer) {
        try {
            // 执行http请求
            return executeRequest(httpUriRequestSupplier.get()
                    , () -> Optional.ofNullable(param).map(m -> JSONObject.from(m).toJSONString()).orElse(null)
                    , responseConsumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送 psot 请求
     *
     * @param url 请求 url
     * @return String
     * @author xijieyin <br> 2022/8/5 19:39
     * @since 1.0.0
     */
    public static String doPost(String url) {
        return doPost(url, null, null);
    }

    /**
     * 发送 JSON 请求<br>
     * 调用的时候可以这样调用：<br>
     * <pre>
     * HttpClientUtil.doRequestJson("<a href="http://xxx">...</a>",null,HttpClientUtil::Post,"");
     * </pre>
     *
     * @param url                    请求 url
     * @param headers                请求头
     * @param param                  请求的参数（url 参数）
     * @param httpUriRequestFunction 请求，发送不同的请求类型的请求 <br>
     *                               {@link #Post}<br>
     *                               {@link #Put}<br>
     *                               {@link #Patch}
     * @param json                   JSON 数据，请求体
     * @return String
     * @author xijieyin <br> 2022/8/5 19:40
     * @since 1.0.0
     */
    public static String doRequestJson(String url
            , Header[] headers
            , Map<String, String> param
            , BiFunction<URI, String
                    , HttpUriRequest> httpUriRequestFunction
            , String json) throws URISyntaxException {
        return doRequestJson(makeUrlParameter(url, param), headers, httpUriRequestFunction, json);
    }

    /**
     * 发送 JSON 请求<br>
     * 调用的时候可以这样调用：<br>
     * <pre>
     * HttpClientUtil.doRequestJson("<a href="http://xxx">...</a>",null,HttpClientUtil::Post,"");
     * </pre>
     *
     * @param url                    请求 url
     * @param headers                请求头
     * @param httpUriRequestFunction 请求，发送不同的请求类型的请求 <br>
     *                               {@link #Post}<br>
     *                               {@link #Put}<br>
     *                               {@link #Patch}
     * @param json                   JSON 数据，请求体
     * @return String
     * @author xijieyin <br> 2022/8/5 19:40
     * @since 1.0.0
     */
    public static String doRequestJson(String url, Header[] headers, BiFunction<URI, String, HttpUriRequest> httpUriRequestFunction, String json) {
        return doRequestJson(URI.create(url), headers, httpUriRequestFunction, json);
    }

    /**
     * 发送 JSON 请求<br>
     * 调用的时候可以这样调用：<br>
     * <pre>
     * HttpClientUtil.doRequestJson("<a href="http://xxx">...</a>",null,HttpClientUtil::Post,"");
     * </pre>
     *
     * @param uri                    请求 url
     * @param headers                请求头
     * @param httpUriRequestFunction 请求，发送不同的请求类型的请求 <br>
     *                               {@link #Post}<br>
     *                               {@link #Put}<br>
     *                               {@link #Patch}
     * @param json                   JSON 数据，请求体
     * @return String
     * @author xijieyin <br> 2022/8/5 19:40
     * @since 1.0.0
     */
    public static String doRequestJson(URI uri
            , Header[] headers
            , BiFunction<URI, String, HttpUriRequest> httpUriRequestFunction
            , String json) {
        return doRequestJson(uri, headers, httpUriRequestFunction, json, closeableHttpResponse -> {
        });
    }

    /**
     * 发送 JSON 请求<br>
     * 调用的时候可以这样调用：<br>
     * <pre>
     * HttpClientUtil.doRequestJson("<a href="http://xxx">...</a>",null,HttpClientUtil::Post,"");
     * </pre>
     *
     * @param uri                    请求 url
     * @param headers                请求头
     * @param httpUriRequestFunction 请求，发送不同的请求类型的请求 <br>
     *                               {@link #Post}<br>
     *                               {@link #Put}<br>
     *                               {@link #Patch}
     * @param json                   JSON 数据，请求体
     * @param responseConsumer       消费 response
     * @return String
     */
    public static String doRequestJson(URI uri
            , Header[] headers
            , BiFunction<URI, String, HttpUriRequest> httpUriRequestFunction
            , String json
            , Consumer<CloseableHttpResponse> responseConsumer) {
        try {
            // 创建Http Post请求
            HttpUriRequest httpPost = httpUriRequestFunction.apply(uri, json);
            // 设置请求头
            httpPost.setHeaders(headers);
            httpPost.setHeader("Connection", "Keep-Alive");
            // 执行http请求
            return executeRequest(httpPost, () -> json, responseConsumer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON POST 请求
     *
     * @param uri  请求地址
     * @param json JSON 数据
     */
    @SneakyThrows
    public static HttpUriRequest Post(URI uri, String json) {
        // 设置请求头
        HttpPost httpUriRequest = new HttpPost(uri);
        // 创建请求内容
        httpUriRequest.setHeader("HTTP Method", "POST");
        if (json != null) {
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpUriRequest.setEntity(entity);
        }
        httpUriRequest.setConfig(RequestConfig.custom().build());
        return httpUriRequest;
    }

    /**
     * JSON Patch 请求
     *
     * @param uri  请求地址
     * @param json JSON 数据
     */
    @SneakyThrows
    public static HttpUriRequest Patch(URI uri, String json) {
        // 设置请求头
        HttpPatch httpUriRequest = new HttpPatch(uri);
        // 创建请求内容
        httpUriRequest.setHeader("HTTP Method", "PATCH");
        if (json != null) {
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpUriRequest.setEntity(entity);
        }
        httpUriRequest.setConfig(RequestConfig.custom().build());
        return httpUriRequest;
    }

    /**
     * JSON Put 请求
     *
     * @param uri  请求地址
     * @param json JSON 数据
     */
    @SneakyThrows
    public static HttpUriRequest Put(URI uri, String json) {
        // 设置请求头
        HttpPut httpUriRequest = new HttpPut(uri);
        httpUriRequest.setHeader("HTTP Method", "PUT");
        if (json != null) {
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpUriRequest.setEntity(entity);
        }
        httpUriRequest.setConfig(RequestConfig.custom().build());
        return httpUriRequest;
    }

    /**
     * 执行请求
     *
     * @param httpRequest      请求对象
     * @param requestParams    请求参数
     * @param responseConsumer 可以在返回结果之后消费 response
     * @return 字符串
     */
    private static String executeRequest(HttpUriRequest httpRequest
            , Supplier<String> requestParams
            , Consumer<CloseableHttpResponse> responseConsumer) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String scheme;
        URI requestUri;
        try {
            requestUri = httpRequest.getUri();
            scheme = requestUri.getScheme();
        } catch (URISyntaxException e) {
            log.error("请求出错", e);
            throw new RuntimeException(e);
        }
        // 创建Httpclient对象
        CloseableHttpClient httpClient = scheme.equalsIgnoreCase("https") ?
                HttpClientBuilder.create().setConnectionManager(getHttpClientConnectionManager()).build() : HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            String requestMethod = httpRequest.getMethod();
            // 构建成一条长 日志，避免并发下日志错乱
            StringBuilder beforeReqLog = new StringBuilder(300);
            // 日志参数
            List<Object> beforeReqArgs = new ArrayList<>();

            // 打印路由
            beforeReqLog.append("\r\n\r\n请求开始：===> {}: {}");
            beforeReqArgs.add(requestMethod);
            beforeReqArgs.add(requestUri);
            // 打印请求头
            Header[] headers = httpRequest.getHeaders();
            for (Header header : headers) {
                beforeReqLog.append("\r\n===Headers===  {} : {}\n");
                beforeReqArgs.add(header.getName());
                beforeReqArgs.add(header.getValue());
            }
            beforeReqLog.append("\r\n===Params===  \r\n{}\n");
            beforeReqArgs.add(requestParams.get());
            beforeReqLog.append("\r\n===CURL===  \r\n{}\n");
            beforeReqArgs.add(getCurl(httpRequest));
            log.debug(beforeReqLog.toString(), beforeReqArgs.toArray());

            // 执行请求
            response = httpClient.execute(httpRequest);
            responseConsumer.accept(response);
            int statusCode = response.getCode();
            String resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            log.debug("\r\n\r\n请求结束 <=== {}: {} \r\n状态码：{}，请求结果：{}\n", requestMethod, requestUri, statusCode, resultString);
            return resultString;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            } catch (IOException e) {
                log.error("httpclient 关闭失败！", e);
            }
        }
    }

    /**
     * 连接配置
     *
     * @return 连接配置
     */
    private static HttpClientConnectionManager getHttpClientConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(getSslConnectionSocketFactory())
                .build();
    }

    /**
     * 支持SSL
     *
     * @return SSLConnectionSocketFactory
     */
    private static SSLConnectionSocketFactory getSslConnectionSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        return new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
    }
}
