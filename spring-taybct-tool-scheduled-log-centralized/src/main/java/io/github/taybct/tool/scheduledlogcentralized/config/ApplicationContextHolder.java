package io.github.taybct.tool.scheduledlogcentralized.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationContextHolder implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        CONTEXT = applicationContext;
    }

    public static ApplicationContext getContext() {
        return CONTEXT;
    }

    public static <T> T getBean(Class<T> clazz) {
        boolean injected = CONTEXT == null;
        if (injected) {
            log.error("context is not injected");
            return null;
        }else {
            return CONTEXT.getBean(clazz);
        }
    }
}