package io.github.mangocrisp.spring.taybct.tool.core.util;

import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.JsonbHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ClassUtils;

/**
 * 获取到消息转换器？这个是复制的 spring 的源码。{@code org.springframework.security.oauth2.core.http.converter.HttpMessageConverters}
 *
 * @author xijieyin <br> 2022/12/29 19:08
 */
public class HttpMessageConverters {
    private static final boolean jackson2Present;
    private static final boolean gsonPresent;
    private static final boolean jsonbPresent;

    private HttpMessageConverters() {
    }

    public static GenericHttpMessageConverter<Object> getJsonMessageConverter() {
        if (jackson2Present) {
            return new MappingJackson2HttpMessageConverter();
        } else if (gsonPresent) {
            return new GsonHttpMessageConverter();
        } else {
            return jsonbPresent ? new JsonbHttpMessageConverter() : null;
        }
    }

    static {
        ClassLoader classLoader = HttpMessageConverters.class.getClassLoader();
        jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", classLoader) && ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", classLoader);
        gsonPresent = ClassUtils.isPresent("com.google.gson.Gson", classLoader);
        jsonbPresent = ClassUtils.isPresent("jakarta.json.bind.Jsonb", classLoader);
    }
}
