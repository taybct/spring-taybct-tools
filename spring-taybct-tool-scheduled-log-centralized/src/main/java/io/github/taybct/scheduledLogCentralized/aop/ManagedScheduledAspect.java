package io.github.taybct.scheduledLogCentralized.aop;


import io.github.taybct.scheduledLogCentralized.annotation.ManagedScheduled;
import io.github.taybct.scheduledLogCentralized.collector.ScheduledLogCollector;
import io.github.taybct.scheduledLogCentralized.context.ScheduledRunContext;
import io.github.taybct.scheduledLogCentralized.util.ScheduledErrorHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
public class ManagedScheduledAspect {

    @Autowired
    private ScheduledLogCollector collector;
    @Autowired
    private Environment environment;

    @Around("@annotation(managedScheduled)")
    public Object around(ProceedingJoinPoint pjp, ManagedScheduled managedScheduled)
            throws Throwable {

        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        String runId = UUID.randomUUID().toString();
        String module = environment.getProperty("spring.application.name");
        String name = managedScheduled.name().isEmpty()
                ? method.getName()
                : managedScheduled.name();

        try {
            ScheduledRunContext.start(runId, module, name);
            return pjp.proceed();
        } catch (Throwable t) {
            ScheduledErrorHolder.record(t);
            throw t;
        } finally {
            collector.collect(runId);
            ScheduledRunContext.clear();
        }
    }

}