package io.github.mangocrisp.spring.taybct.tool.core.aop;

import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.WebLog;
import io.github.mangocrisp.spring.taybct.tool.core.constant.AppConstants;
import io.github.mangocrisp.spring.taybct.tool.core.util.AOPUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 打印请求日志，主要用于调试查看请求参数
 *
 * @author xijieyin <br> 2022/8/5 13:46
 * @see WebLog
 * @since 1.0.0
 */
@Aspect
@AutoConfiguration
@Slf4j
@Profile({AppConstants.DEV_CODE, AppConstants.TEST_CODE})
public class WebLogAspect {

    @Pointcut(
            "@annotation(io.github.mangocrisp.spring.taybct.tool.core.annotation.WebLog) "
//                   + " && " +
//            " ( @within(org.springframework.stereotype.Controller) " +
//            "|| @within(org.springframework.web.bind.annotation.RestController) )"
    )
    public void logPointCut() {
    }


    @Before("logPointCut()")
    public void doBefore(JoinPoint point) throws Throwable {

        // 请求参数处理
        final JSONObject paraMap = AOPUtil.getParams(point);

        HttpServletRequest request = ServletUtil.getRequest();
        String requestURI = Objects.requireNonNull(request).getRequestURI();
        String requestMethod = request.getMethod();

        // 构建成一条长 日志，避免并发下日志错乱
        StringBuilder beforeReqLog = new StringBuilder(300);
        // 日志参数
        List<Object> beforeReqArgs = new ArrayList<>();
        beforeReqLog.append("\n\n================  Request Start  ================\n");
        // 打印路由
        beforeReqLog.append("===> {}: {}");
        beforeReqArgs.add(requestMethod);
        beforeReqArgs.add(requestURI);
        // 请求参数
        if (paraMap.isEmpty()) {
            beforeReqLog.append("\n");
        } else {
            beforeReqLog.append(" Parameters: {}\n");
            try {
                beforeReqArgs.add(paraMap.toJSONString());
            } catch (Exception e) {
                beforeReqArgs.add(e.getMessage());
            }
        }
        // 打印请求头
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            String headerValue = request.getHeader(headerName);
            beforeReqLog.append("===Headers===  {} : {}\n");
            beforeReqArgs.add(headerName);
            beforeReqArgs.add(headerValue);
        }
        beforeReqLog.append("================  Request End   ================\n");
        log.debug(beforeReqLog.toString(), beforeReqArgs.toArray());
    }

    @AfterReturning(returning = "ret", pointcut = "logPointCut()")// returning的值和doAfterReturning的参数名一致
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容(返回值太复杂时，打印的是物理存储空间的地址)
//        log.trace("返回值 : " + ret);
    }

    @Around("logPointCut()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        HttpServletRequest request = ServletUtil.getRequest();
        String requestURI = Objects.requireNonNull(request).getRequestURI();
        // 打印执行时间
        long startNs = System.nanoTime();
        // aop 执行后的日志
        StringBuilder afterReqLog = new StringBuilder(200);
        // 日志参数
        List<Object> afterReqArgs = new ArrayList<>();
        afterReqLog.append("\n\n================  Response Start  ================\n");
        try {
            return point.proceed();
        } finally {
            String requestMethod = request.getMethod();
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
            afterReqLog.append("<=== {}: {} ({} ms)\n");
            afterReqArgs.add(requestMethod);
            afterReqArgs.add(requestURI);
            afterReqArgs.add(tookMs);
            afterReqLog.append("================  Response End   ================\n");
            log.debug(afterReqLog.toString(), afterReqArgs.toArray());
        }
    }
}
