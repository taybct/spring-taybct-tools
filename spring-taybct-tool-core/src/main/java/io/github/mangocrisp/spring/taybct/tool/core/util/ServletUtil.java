package io.github.mangocrisp.spring.taybct.tool.core.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * servlet 工具类
 *
 * @author xijieyin <br> 2022/8/5 19:49
 * @since 1.0.0
 */
public class ServletUtil {

    /**
     * 获取请求的 ip 地址
     *
     * @return String
     * @author xijieyin <br> 2022/8/5 19:49
     * @since 1.0.0
     */
    public static String getIpAddr() {
        return getIpAddr(getRequest());
    }

    /**
     * 获取请求的 ip 地址
     *
     * @param request 请求对象
     * @return String
     * @author xijieyin <br> 2022/8/5 19:50
     * @since 1.0.0
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = null;

        // X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            // X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        // 有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        // 还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
    }

    /**
     * 获取 request
     *
     * @return HttpServletRequest
     * @author xijieyin <br> 2022/8/5 19:50
     * @since 1.0.0
     */
    public static HttpServletRequest getRequest() {
        try {
            return Objects.requireNonNull(getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取 response
     *
     * @return HttpServletResponse
     * @author xijieyin <br> 2022/8/5 19:50
     * @since 1.0.0
     */
    public static HttpServletResponse getResponse() {
        try {
            return Objects.requireNonNull(getRequestAttributes()).getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取请求头
     *
     * @param request 请求对象
     * @return Map&lt;String&gt;
     * @author xijieyin <br> 2022/8/5 19:51
     * @since 1.0.0
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeaderNames())
                .map(enumeration -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    while (enumeration.hasMoreElements()) {
                        String key = enumeration.nextElement();
                        String value = request.getHeader(key);
                        map.put(key, value);
                    }
                    return map;
                }).orElseGet(LinkedHashMap::new);
    }

    /**
     * 编码内容
     *
     * @param str 编码前的内容
     * @return String
     * @author xijieyin <br> 2022/8/5 19:51
     * @since 1.0.0
     */
    public static String urlEncode(String str) throws UnsupportedEncodingException {
        return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
    }


    /**
     * 内容解码
     *
     * @param str 解码的内容
     * @return String
     * @author xijieyin <br> 2022/8/5 19:51
     * @since 1.0.0
     */
    public static String urlDecode(String str) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
    }

    /**
     * 获取响应
     *
     * @param response response 对象
     * @param status   状态
     * @param json     需要输出的 json
     * @author xijieyin <br> 2022/8/5 19:52
     * @since 1.0.0
     */
    public static void genResponse(HttpServletResponse response, HttpStatus status, String json) throws IOException {
        response.setStatus(status.value());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().print(json);
        response.getWriter().flush();
    }
}
