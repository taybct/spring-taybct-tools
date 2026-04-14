package io.github.taybct.tool.core.util;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public static List<? extends Class<?>> scanWithSpring(String basePackage, Class<? extends Annotation> annotation) {
        // Spring 内置扫描器
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        // 添加注解过滤条件
        scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));

        return scanner.findCandidateComponents(basePackage).stream()
                .map(beanDefinition -> {
                    try {
                        return Class.forName(beanDefinition.getBeanClassName());
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .toList();
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
