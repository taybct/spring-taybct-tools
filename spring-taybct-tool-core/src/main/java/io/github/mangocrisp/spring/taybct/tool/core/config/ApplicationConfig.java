package io.github.mangocrisp.spring.taybct.tool.core.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.constant.ISysParamsObtainService;
import io.github.mangocrisp.spring.taybct.tool.core.constant.PropertyParams;
import io.github.mangocrisp.spring.taybct.tool.core.enhance.DefaultMethodEnhanceHandler;
import io.github.mangocrisp.spring.taybct.tool.core.enhance.EnDecryptedHandler;
import io.github.mangocrisp.spring.taybct.tool.core.exception.handler.DefaultExceptionPrinter;
import io.github.mangocrisp.spring.taybct.tool.core.exception.handler.DefaultExceptionReporter;
import io.github.mangocrisp.spring.taybct.tool.core.exception.handler.IGlobalExceptionReporter;
import io.github.mangocrisp.spring.taybct.tool.core.exception.handler.IGlobalPrinter;
import io.github.mangocrisp.spring.taybct.tool.core.handle.TableFieldDefaultLoginUserIdHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.TableFieldDefaultLoginUsernameHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.TableFieldDefaultPKHandler;
import io.github.mangocrisp.spring.taybct.tool.core.handle.TableFieldDefaultUKHandler;
import io.github.mangocrisp.spring.taybct.tool.core.interceptor.MethodEnhanceInterceptor;
import io.github.mangocrisp.spring.taybct.tool.core.message.MessageProperties;
import io.github.mangocrisp.spring.taybct.tool.core.support.IEncryptedPassable;
import io.github.mangocrisp.spring.taybct.tool.core.util.rsa.RSACoder;
import io.github.mangocrisp.spring.taybct.tool.core.util.sm.SM2Coder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 系统配置
 *
 * @author xijieyin <br> 2022/8/5 17:43
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties({GlobalExceptionConfig.class
        , PropConfig.class
        , PropertyParams.class
        , ControllerRegisterConfigure.class
        , MessageProperties.class})
@Slf4j
public class ApplicationConfig {

    /**
     * 系统参数获取接口
     *
     * @return ISysParamsObtainService
     */
    @Bean
    @ConditionalOnMissingBean
    public ISysParamsObtainService sysParamsObtainService(PropertyParams propertyParams) {
        return key -> propertyParams.getMap().get(key);
    }

    /**
     * 时区配置
     *
     * @return Jackson2ObjectMapperBuilderCustomizer
     * @author xijieyin <br> 2022/8/5 17:43
     * @since 1.0.0
     */
    @Bean
    @ConditionalOnMissingBean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jackson2HttpMessageConverter.getObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

        // 后台Long值传递给前端精度丢失问题（JS最大精度整数是Math.pow(2,53)）
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        // simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);

        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
                // .dateFormat(new SimpleDateFormat(DateConstants.format.YYYY_MM_DD_HH_mm_ss))
                // .timeZone(TimeZone.getDefault())
                // .modules(simpleModule)
                .configure(objectMapper);
    }

    /**
     * 全局异常捕获记录器
     *
     * @return IGlobalExceptionReporter
     */
    @Bean
    @ConditionalOnMissingBean
    public IGlobalExceptionReporter globalExceptionReporter() {
        return new DefaultExceptionReporter();
    }

    /**
     * 全局捕获输出器，需要怎么输出给请求端
     *
     * @param globalExceptionConfig 异常信息配置
     * @return IGlobalPrinter
     */
    @Bean
    @ConditionalOnMissingBean
    public IGlobalPrinter globalPrinter(
            GlobalExceptionConfig globalExceptionConfig
    ) {
        return new DefaultExceptionPrinter(globalExceptionConfig);
    }

    /**
     * 安全工具类
     *
     * @return ISecurityUtil
     */
    @Bean
    @ConditionalOnMissingBean
    public ISecurityUtil securityUtil() {
        return () -> null;
    }

    @Bean
    @ConditionalOnMissingBean
    public TableFieldDefaultPKHandler tableFieldDefaultPKHandler() {
        return new TableFieldDefaultPKHandler() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TableFieldDefaultUKHandler tableFieldDefaultUKHandler() {
        return new TableFieldDefaultUKHandler() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TableFieldDefaultLoginUserIdHandler tableFieldDefaultLoginUserIdHandler() {
        return new TableFieldDefaultLoginUserIdHandler() {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TableFieldDefaultLoginUsernameHandler tableFieldDefaultLoginUsernameHandler() {
        return new TableFieldDefaultLoginUsernameHandler() {
        };
    }

    @Bean
    public DefaultMethodEnhanceHandler defaultMethodEnhanceHandler() {
        DefaultMethodEnhanceHandler defaultMethodEnhanceHandler = new DefaultMethodEnhanceHandler();
        defaultMethodEnhanceHandler.add(new EnDecryptedHandler());
        return defaultMethodEnhanceHandler;
    }

    @Bean
    public DefaultPointcutAdvisor methodEnhanceAdvisor(DefaultMethodEnhanceHandler defaultMethodEnhanceHandler) {
        MethodEnhanceInterceptor methodInterceptor = new MethodEnhanceInterceptor(defaultMethodEnhanceHandler.getHandlerList());
        // 匹配一个切点，这里使用注解
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceMethod)");
        // 增强
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(pointcut);
        // 增强的方法
        advisor.setAdvice(methodInterceptor);
        return advisor;
    }

    /**
     * 加密校验，默认支持 sm2 和 rsa
     *
     * @return 加密校验
     */
    @Bean
    @ConditionalOnMissingBean(IEncryptedPassable.class)
    IEncryptedPassable encryptedPassable() {
        return s -> {
            try {
                if (s.startsWith("04")) {
                    // 如果加密是 04 开头就是 sm2 加密
                    return SM2Coder.decryptWebData(s);
                }
            } catch (Exception e) {
                log.trace("sm2 解密失败", e);
            }
            // 不然就是 RSA
            return RSACoder.decryptBase64StringByPrivateKey(s);
        };
    }

}
