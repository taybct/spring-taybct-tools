package io.github.taybct.scheduledLogCentralized.util;

import io.github.taybct.scheduledLogCentralized.config.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class ModuleNameUtil {
    private ModuleNameUtil() {}

    public static String getModuleName() {
        ApplicationContext ctx = ApplicationContextHolder.getContext();
        if (ctx == null) {
            return "UNKNOWN";
        }

        Environment env = ctx.getEnvironment();
        return env.getProperty("spring.application.name", "UNKNOWN");
    }
}
