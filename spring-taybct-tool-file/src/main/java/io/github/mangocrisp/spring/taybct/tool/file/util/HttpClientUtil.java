package io.github.mangocrisp.spring.taybct.tool.file.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Http Client 请求工具类，主要用来做代理请求
 *
 * @author xijieyin <br> 2022/8/4 16:58
 * @since 1.0.0
 */
@Slf4j
public class HttpClientUtil {

    /**
     * post请求，带json参数
     *
     * @param url  请求地址 url
     * @param json 请求体
     * @return 请求返回结果
     */
    public static String sendJsonPost(String url, String json) {
        //1.创建httpClient
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //2.创建post请求方式实例
        HttpPost httpPost = new HttpPost(url);

        //2.1设置请求头 发送的是json数据格式
        httpPost.setHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Connection", "Close");
        //3.设置参数---设置消息实体 也就是携带的数据
        StringBuilder line = new StringBuilder();
        try {
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            entity.setContentEncoding("UTF-8");  //设置编码格式
            // 发送Json格式的数据请求
            entity.setContentType("application/json");
            //把请求消息实体塞进去
            httpPost.setEntity(entity);

            //4.执行http的post请求
            CloseableHttpResponse httpResponse;
            InputStream inputStream;
            httpResponse = httpClient.execute(httpPost);
            //5.对返回的数据进行处理
            //5.1判断是否成功
//            log.debug(httpResponse.getStatusLine().getStatusCode()+"");

            //5.2对数据进行处理
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent(); //获取content实体内容
            //封装成字符流来输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String content;
            while ((content = bufferedReader.readLine()) != null) {
                line.append(content);
//				log.debug("line:"+line);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return line.toString();
    }

    /**
     * GET请求
     *
     * @param url 请求地址
     * @return 请求返回结果
     */
    public static String sendGet(String url, Map<String, String> map) {
        String result;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            //创建uri
            URIBuilder builder = new URIBuilder(url);
            if (map != null) {
                map.forEach(builder::addParameter);
            }
            URI uri = builder.build();
            //创建get请求
            HttpGet httpGet = new HttpGet(uri);
            //执行请求
            response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");

            } else {
                result = "请求失败";
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
                if (response != null) {
                    response.close();
                }
            } catch (Exception e) {
                log.error("response 关闭失败！", e);
            }

        }
        return result;
    }

    /**
     * 代理方法
     *
     * @author xijieyin <br> 2022/8/4 17:00
     * @since 1.0.0
     */
    public interface ProxyMethod {
        /**
         * 测试连接
         */
        String TEST_CONNECTION = "testConnection";
    }

    /**
     * <p>代理请求</p>
     *
     * @param proxyUrl 代理地址
     * @param method   请求方法
     * @param message  请求参数
     * @author xijieyin <br> 2022/8/4 17:00
     */
    public static String proxy(String proxyUrl, String method, String message) {
        String response = sendGet(String.format("%s/%s", proxyUrl, ProxyMethod.TEST_CONNECTION), null);
        if (response.equalsIgnoreCase("200")) {
            return sendJsonPost(String.format("%s/%s", proxyUrl, method), message);
        } else {
            return null;
        }
    }

}
