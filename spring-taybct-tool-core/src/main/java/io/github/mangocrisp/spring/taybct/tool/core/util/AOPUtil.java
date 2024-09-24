package io.github.mangocrisp.spring.taybct.tool.core.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 切面工具类
 *
 * @author xijieyin <br> 2022/8/5 19:16
 * @since 1.0.0
 */
public class AOPUtil {

    /**
     * SPEL表达式标识符
     */
    public final static String SPEL_FLAG = "#";

    /**
     * 获取到 key 对应的字段的位置
     *
     * @param point 切点
     * @param key   字段名
     */
    public static int getParamIndex(ProceedingJoinPoint point, String key) {
        // 不符合EL表达式占位符，原值返回
        if (StringUtils.isBlank(key)) {
            return -1;
        }
        // 获取到方法
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String[] parameterNames = ArrayUtil.toArray(Arrays.stream(method.getParameters())
                .map(Parameter::getName).collect(Collectors.toList()), String.class);
        for (int i = 0; i < Objects.requireNonNull(parameterNames).length; i++) {
            if (key.equalsIgnoreCase(parameterNames[i])) {
                return i;
            }
        }
        return -1;
//        return SpelParser.getValue(key,parameterNames,point.getArgs());
    }

    /**
     * 获取切点参数
     *
     * @param point 切点
     */
    public static JSONObject getParams(JoinPoint point) {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        Object[] args = point.getArgs();
        return getJsonObject(method, args);
    }

    /**
     * 获取切点参数
     *
     * @param methodInvocation 切点
     */
    public static JSONObject getParams(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        Object[] args = methodInvocation.getArguments();
        return getJsonObject(method, args);
    }

    @NotNull
    private static JSONObject getJsonObject(Method method, Object[] args) {
        // 请求参数处理
        final JSONObject paraMap = new JSONObject();
        for (int i = 0; i < args.length; i++) {
            // 读取方法参数
            MethodParameter methodParam = ClassUtil.getMethodParameter(method, i);
            // PathVariable 参数跳过
            PathVariable pathVariable = methodParam.getParameterAnnotation(PathVariable.class);
            if (pathVariable != null) {
                continue;
            }
            RequestBody requestBody = methodParam.getParameterAnnotation(RequestBody.class);
            Object value = args[i];
            // 如果是body的json则是对象
            if (requestBody != null && value != null) {
                paraMap.put("Array:", JSON.toJSONString(value));
                continue;
            }
            // 处理 List
            if (value instanceof List) {
                value = ((List) value).get(0);
            }
            // 处理 参数
            if (value instanceof HttpServletRequest) {
                paraMap.putAll(((HttpServletRequest) value).getParameterMap());
            } else if (value instanceof WebRequest) {
                paraMap.putAll(((WebRequest) value).getParameterMap());
            } else if (value instanceof MultipartFile) {
                MultipartFile multipartFile = (MultipartFile) value;
                String name = multipartFile.getName();
                String fileName = multipartFile.getOriginalFilename();
                paraMap.put(name, fileName);
            } else if (value instanceof HttpServletResponse) {
            } else if (value instanceof InputStream) {
            } else if (value instanceof InputStreamSource) {
            } else {
                // 参数名
                RequestParam requestParam = methodParam.getParameterAnnotation(RequestParam.class);
                String paraName;
                if (requestParam != null && StringUtils.isNotBlank(requestParam.value())) {
                    paraName = requestParam.value();
                } else {
                    paraName = methodParam.getParameterName();
                }
                paraMap.put(paraName, value);
            }
        }
        return paraMap;
    }

    /**
     * 解析SPEL表达式
     *
     * @param spELString       表达式
     * @param methodInvocation 切点
     * @return 解析后的结果
     */
    public static String generateKeyBySpEL(String spELString, MethodInvocation methodInvocation) {
        return generateKeyBySpEL(spELString, methodInvocation, () -> null);
    }

    /**
     * 解析SPEL表达式
     *
     * @param spELString       表达式
     * @param methodInvocation 切点
     * @param result           返回结果
     * @return 解析后的结果
     */
    public static String generateKeyBySpEL(String spELString, MethodInvocation methodInvocation, @NonNull Supplier<Object> result) {
        if (!StrUtil.contains(spELString, SPEL_FLAG)) {
            return spELString;
        }
        // 用于获取方法参数定义名字.
        DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
        String[] paramNames = nameDiscoverer.getParameterNames(methodInvocation.getMethod());
        // 用于SpEL表达式解析.
        SpelExpressionParser parser = new SpelExpressionParser();
        // 解析后的SPEL
        Expression expression = parser.parseExpression(spELString);
        // spring表达式上下文
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = methodInvocation.getArguments();
        // 给上下文赋值变量
        for (int i = 0; i < args.length; i++) {
            assert paramNames != null;
            context.setVariable(paramNames[i], args[i]);
        }
        if (result.get() != null) {
            context.setVariable("result", result.get());
        }
        return Optional.ofNullable(expression.getValue(context)).map(Object::toString).orElse("");
    }

}
