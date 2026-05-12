package io.github.taybct.tool.core.util;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 *
 * <pre>
 * Spring 注解扫描器
 * </pre>
 *
 * @author XiJieYin
 * @since 2026/4/7 16:58
 */
@Slf4j
public class AnnotationScanner {

    /**
     *
     * <pre>
     * 扫描指定包下的指定注解的类
     * </pre>
     *
     * @param basePackage   包名
     * @param annotation    注解
     * @return List&lt;? extends Class&lt;?&gt;&gt;
     */
    public static List<Class<?>> scanWithSpring(
            String basePackage,
            Class<? extends Annotation> annotation
    ) {

        ApplicationContext ctx = SpringUtil.getContext();

        ConditionContext context = new ConditionContext() {
            @Override
            public BeanDefinitionRegistry getRegistry() {
                return (BeanDefinitionRegistry) ctx.getAutowireCapableBeanFactory();
            }

            @Override
            public @Nullable ConfigurableListableBeanFactory getBeanFactory() {
                return (ConfigurableListableBeanFactory) ctx.getAutowireCapableBeanFactory();
            }

            @Override
            public Environment getEnvironment() {
                return ctx.getEnvironment();
            }

            @Override
            public org.springframework.core.io.ResourceLoader getResourceLoader() {
                return ctx;
            }

            @Override
            public ClassLoader getClassLoader() {
                return ctx.getClassLoader();
            }
        };

        // 1. 校验
        if (!StringUtils.hasText(basePackage)) {
            throw new IllegalArgumentException("basePackage 不能为空");
        }
        Objects.requireNonNull(annotation, "annotation 不能为 null");
        Objects.requireNonNull(context, "ConditionContext 不能为 null");

        // 2. 扫描器
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));

        List<Class<?>> result = new ArrayList<>();
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

        // 3. 遍历并手动判断 @Conditional
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            String className = bd.getBeanClassName();
            Class<? extends BeanDefinition> aClass = bd.getClass();
            if (!StringUtils.hasText(className)) {
                continue;
            }

            try {
                // 获取类上的 @Conditional 元数据
                AnnotationMetadata metadata = AnnotationMetadata.introspect(aClass);
                Map<String, Object> conditionalAttrs = metadata.getAnnotationAttributes(Conditional.class.getName());

                boolean skip = false;
                if (conditionalAttrs != null) {
                    // 拿到所有 Condition 实现类
                    Class<? extends Condition>[] conditionClasses =
                            (Class<? extends Condition>[]) conditionalAttrs.get("value");
                    for (Class<? extends Condition> condClass : conditionClasses) {
                        Condition condition = ClassUtil.createInstanceWithSpring(condClass);
                        // 手动执行匹配
                        if (!condition.matches(context, metadata)) {
                            skip = true;
                            break;
                        }
                    }
                }

                // 条件全部满足才加入结果
                if (!skip) {
                    Class<?> clazz = ClassUtils.forName(className, classLoader);
                    result.add(clazz);
                }
            } catch (Exception e) {
                // 跳过异常类
            }
        }
        return result;
    }

    /**
     *
     * <pre>
     * 扫描指定包下的指定注解的类
     * </pre>
     *
     * @param packageName   包名
     * @param annotation    注解
     * @return Set&lt;Class&lt;?&gt;&gt;
     */
    public static Set<Class<?>> scan(String packageName, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    /**
     * 扫描指定包下，带有指定注解的所有类
     * @param packageName 包名（如：com.example.demo）
     * @param annotation 注解类
     * @return 标注了该注解的类列表
     */
    public static List<Class<?>> scanAnnotatedClasses(String packageName, Class<? extends Annotation> annotation) {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        // 把包名转换为文件路径
        String packagePath = packageName.replace('.', '/');
        // 获取类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);

        if (resource == null) {
            return annotatedClasses;
        }

        try {
            File packageDir = new File(resource.toURI());
            // 递归扫描目录下的所有 class 文件
            scanDirectory(packageDir, packageName, annotation, annotatedClasses);
        } catch (URISyntaxException e) {
            log.error("扫描包失败！", e);
        }

        return annotatedClasses;
    }

    // 递归扫描文件目录
    private static void scanDirectory(File dir, String packageName, Class<? extends Annotation> annotation, List<Class<?>> result) {
        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // 递归子包
                String subPackage = packageName + "." + file.getName();
                scanDirectory(file, subPackage, annotation, result);
            } else if (file.getName().endsWith(".class")) {
                // 加载类
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    // 判断类上是否有目标注解
                    if (clazz.isAnnotationPresent(annotation)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException e) {
                    // 忽略无法加载的类
                }
            }
        }
    }
}
