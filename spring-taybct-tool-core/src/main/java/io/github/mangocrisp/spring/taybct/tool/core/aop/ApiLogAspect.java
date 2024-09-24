package io.github.mangocrisp.spring.taybct.tool.core.aop;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.ApiLog;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ILoginUser;
import io.github.mangocrisp.spring.taybct.tool.core.bean.ISecurityUtil;
import io.github.mangocrisp.spring.taybct.tool.core.enums.OperateStatus;
import io.github.mangocrisp.spring.taybct.tool.core.message.IMessageSendService;
import io.github.mangocrisp.spring.taybct.tool.core.message.MessageProperties;
import io.github.mangocrisp.spring.taybct.tool.core.message.apilog.ApiLogDTO;
import io.github.mangocrisp.spring.taybct.tool.core.util.AOPUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.ServletUtil;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * 操作日志记录处理
 *
 * @author xijieyin <br> 2022/8/5 20:16
 * @since 1.0.0
 */
@Aspect
@AutoConfiguration
@Slf4j
@RequiredArgsConstructor
public class ApiLogAspect {

    final IMessageSendService apiLogSendService;

    final MessageProperties messageProperties;

    @Value("${spring.application.name}")
    private String module;

    @Resource
    private ISecurityUtil securityUtil;

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(io.github.mangocrisp.spring.taybct.tool.core.annotation.ApiLog)")
    public void logPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param point 切点
     */
    @AfterReturning(pointcut = "logPointCut()", returning = "result")
    public void doAfterReturning(JoinPoint point, Object result) {
        handleLog(point, null, result);
    }

    /**
     * 拦截异常操作
     *
     * @param point 切点
     * @param e     异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint point, Exception e) {
        handleLog(point, e, null);
    }

    @Async
    protected void handleLog(final JoinPoint point, final Exception e, Object result) {
        if (!messageProperties.getEnable()) {
            return;
        }
        try {
            // 获得注解
            Optional<ApiLog> optionalApiLog = getAnnotationLog(point);
            optionalApiLog.ifPresent(controllerLog -> {
                ApiLogDTO apiLog = new ApiLogDTO();
                // 创建时间
                apiLog.setCreateTime(LocalDateTime.now());
                // 设置标题
                apiLog.setTitle(controllerLog.title());
                // 设置描述
                apiLog.setDescription(controllerLog.description());

                // 模块
                apiLog.setModule(module);
                // 请求 主机地址
                apiLog.setIp(ServletUtil.getIpAddr());
                // 业务类型
                apiLog.setType(controllerLog.type());
                // 设置请求方式
                apiLog.setMethod(Objects.requireNonNull(ServletUtil.getRequest()).getMethod());
                // 请求 的 url
                apiLog.setUrl(ServletUtil.getRequest().getRequestURL().toString());

                if (controllerLog.isSaveRequestData()) {
                    // 获取参数
                    JSONObject paraMap = AOPUtil.getParams(point);
                    apiLog.setParams(paraMap.toJSONString());
                }
                // 状态码
                apiLog.setCode(OperateStatus.SUCCESS.getCode());
                // 是否要保存返回结果
                if (controllerLog.isSaveResultData()) {
                    if (e == null) {
                        // 返回参数
                        apiLog.setResult(JSON.toJSONString(result));
                    }
                }
                if (e != null) {
                    // 状态码
                    apiLog.setCode(OperateStatus.FAILED.getCode());
                    apiLog.setResult(StringUtils.substring(e.getMessage(), 0, 2000));
                }

                String username = "anonymous";
                String client = "anonymous";
                String tenantId = "anonymous";
                if (securityUtil != null) {
                    // 获取当前的用户
                    try {
                        ILoginUser loginUser = securityUtil.getLoginUser();
                        if (loginUser.getUsername() != null) {
                            username = loginUser.getUsername();
                        }
                        if (loginUser.getClientId() != null) {
                            client = loginUser.getClientId();
                        }
                        if (loginUser.getTenantId() != null) {
                            tenantId = loginUser.getTenantId();
                        }
                    } catch (Exception ex) {
                        log.trace("没有登录用户信息，只记录接口操作日志【{}】", apiLog.getUrl());
                    }
                }

                apiLog.setUsername(username);
                // 客户端类型
                apiLog.setClient(client);
                // 租户 id
                apiLog.setTenantId(tenantId);

                apiLogSendService.send(apiLog);
            });
        } catch (Exception ex) {
            log.error("==前置通知异常==");
            log.error("异常信息:{}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private Optional<ApiLog> getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return Optional.ofNullable(methodSignature.getMethod())
                .map(method -> method.getAnnotation(ApiLog.class));
    }

}
