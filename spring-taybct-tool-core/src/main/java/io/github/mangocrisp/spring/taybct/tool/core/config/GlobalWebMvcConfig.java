package io.github.mangocrisp.spring.taybct.tool.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.github.mangocrisp.spring.taybct.tool.core.constant.DateConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * 全局 web mvc 配置
 *
 * @author XiJieYin <br> 2023/2/6 10:44
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
public class GlobalWebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册自定义类型转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                // 获取Json转换器
                .filter(converter -> MappingJackson2HttpMessageConverter.class.isAssignableFrom(converter.getClass()))
                .map(converter -> (MappingJackson2HttpMessageConverter) converter)
                .forEach(converter -> {
                    // 默认编码
                    converter.setDefaultCharset(StandardCharsets.UTF_8);
                    // 将自定义序列化器注册进Json转换器中
                    ObjectMapper objectMapper = converter.getObjectMapper();
                    // 默认的日期格式
                    objectMapper.setDateFormat(new SimpleDateFormat(DateConstants.format.YYYY_MM_DD_HH_mm_ss));
                    // 默认的时区
                    objectMapper.setTimeZone(DateConstants.Zone.CHINA);
                    // 允许未加引号的字段名
                    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    // 后台Long值传递给前端精度丢失问题（JS最大精度整数是Math.pow(2,53)）
                    SimpleModule simpleModule = new SimpleModule();
                    // 指定类型转换
                    simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                    // 不能使用，因为一些框架里面的分页都是 long
                    //simpleModule.addSerializer(long.class, ToStringSerializer.instance);

                    // 设置 LocalDateTime 转换
                    simpleModule.addSerializer(LocalDateTime.class
                            , new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DateConstants.format.YYYY_MM_DD_HH_mm_ss, Locale.CHINA)));

                    objectMapper.registerModule(simpleModule);
                });
    }
}
