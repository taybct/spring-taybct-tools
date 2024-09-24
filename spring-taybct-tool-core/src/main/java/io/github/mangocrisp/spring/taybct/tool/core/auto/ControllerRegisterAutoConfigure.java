package io.github.mangocrisp.spring.taybct.tool.core.auto;

import cn.hutool.core.collection.CollectionUtil;
import io.github.mangocrisp.spring.taybct.tool.core.config.ControllerRegisterConfigure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Controller 注册自动配置
 *
 * @author XiJieYin <br> 2023/7/24 11:38
 */
@AutoConfiguration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({ControllerRegisterConfigure.class})
public class ControllerRegisterAutoConfigure implements ApplicationRunner {

    final ControllerRegisterConfigure controllerRegisterConfigure;

    final ApplicationContext applicationContext;

    final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 设置默认的允许的请求类型
        if (CollectionUtil.isEmpty(controllerRegisterConfigure.getAllowMethod())) {
            controllerRegisterConfigure.setAllowMethod(new LinkedHashSet<>(Arrays.asList(GetMapping.class
                    , PostMapping.class
                    , PutMapping.class
                    , PatchMapping.class
                    , DeleteMapping.class
                    , RequestMapping.class)));
        }
        log.debug("注册 Controller 开始");
        Field field = RequestMappingHandlerMapping.class.getDeclaredField("config");
        field.setAccessible(true);
        RequestMappingInfo.BuilderConfiguration configuration = (RequestMappingInfo.BuilderConfiguration) field.get(requestMappingHandlerMapping);
        controllerRegisterConfigure.getRegisterControllerSet().forEach(controllerClass -> {
            RequestMapping classRequestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass, RequestMapping.class);
            if (classRequestMapping == null) {
                return;
            }
            String[] classRequestPath = classRequestMapping.value();
            if (classRequestPath[0].endsWith("/")) {
                classRequestPath[0] = classRequestPath[0].substring(0, classRequestPath[0].length() - 1);
            }
            Arrays.stream(controllerClass.getMethods())
                    .filter(method -> {
                        for (Class<? extends Annotation> aClass : controllerRegisterConfigure.getAllowMethod()) {
                            if (method.isAnnotationPresent(aClass)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .forEach(method -> {
                        RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                        if (requestMapping == null) {
                            return;
                        }
                        String[] path = requestMapping.path();
                        if (path.length > 0 && path[0].length() > 0) {
                            if (path[0].startsWith("/")) {
                                path[0] = classRequestPath[0] + path[0];
                            } else {
                                path[0] = classRequestPath[0] + "/" + path[0];
                            }
                        } else {
                            path = new String[]{classRequestPath[0]};
                        }
                        log.debug("注册方法: {}.{}({})", controllerClass.getName(), method.getName(), path[0]);
                        RequestMappingInfo.Builder builder = RequestMappingInfo
                                .paths(path)
                                .methods(requestMapping.method())
                                .params(requestMapping.params())
                                .headers(requestMapping.headers())
                                .consumes(requestMapping.consumes())
                                .produces(requestMapping.produces())
                                .mappingName(requestMapping.name());
                        builder.options(configuration);
                        // 也可以这样注入
                        try {
                            Constructor<?> declaredConstructor = controllerClass.getDeclaredConstructor();
                            Object bean = declaredConstructor.newInstance();
                            applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);
                            requestMappingHandlerMapping.registerMapping(builder.build(), bean, method);
                        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                                 InvocationTargetException e) {
                            log.error("Controller 注册失败！", e);
                        }
                    });
        });
    }
}
