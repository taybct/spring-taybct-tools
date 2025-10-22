package io.github.taybct.tool.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;

/**
 * spring 工具类
 *
 * @author xijieyin <br> 2022/8/5 22:33
 * @since 1.0.0
 */
@AutoConfiguration
@Slf4j
public class SpringUtil implements BeanFactoryPostProcessor, ApplicationContextAware {
    /**
     * Spring应用上下文环境
     */
    private static ConfigurableListableBeanFactory beanFactory;

    private static ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtil.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtil.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     *
     * @param name bean 名
     * @return Object 一个以所给名字注册的bean的实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) throws BeansException {
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz 类型
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        return beanFactory.getBean(clz);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name bean 名
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name bean 名
     * @return boolean
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name bean 名
     * @return Class 注册对象的类型
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name bean 名
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getAliases(name);
    }

    /**
     * 获取aop代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }

    /**
     * 获取当前的环境配置，无配置返回null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles() {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取当前的环境配置，当有多个环境配置时，只获取第一个
     *
     * @return 当前的环境配置
     */
    public static String getActiveProfile() {
        final String[] activeProfiles = getActiveProfiles();
        return ObjectUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : null;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        if (null == beanName || "".equals(beanName.trim())) {
            return null;
        }
        if (clazz == null) {
            return null;
        }
        return applicationContext.getBean(beanName, clazz);
    }

    public static ApplicationContext getContext() {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext;
    }

    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext == null) {
            return;
        }
        try {
            applicationContext.publishEvent(event);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    /**
     * 注册 bean
     *
     * @param name  自定义 bean name
     * @param clazz 注册的 bean 的类型
     * @param args  构造方法的必要参数，顺序和类型要求和 clazz 中定义的一致
     */
    public static <T> T registerBean(String name, Class<T> clazz, Object... args) {
        if (applicationContext.containsBean(name)) {
            Object bean = applicationContext.getBean(name);
            if (bean.getClass().isAssignableFrom(clazz)) {
                return (T) bean;
            } else {
                throw new RuntimeException("BeanName 重复 " + name);
            }
        }

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        if (args != null) {
            for (Object arg : args) {
                beanDefinitionBuilder.addConstructorArgValue(arg);
            }
        }
        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();

        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) SpringUtil.beanFactory;
        beanDefinitionRegistry.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 注册 bean
     *
     * @param clazz    继承/实现类
     * @param beanName bean 名
     * @return 如果有相同的 bean 名的 bean 就会直接返回这个已经存在的 bean
     */
    public static <T> T registerBean(Class<? extends T> clazz, String beanName) {
        T t = null;
        try {
            t = getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            log.info("初次加载[{}]", beanName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (t == null) {
            registerBean(beanName, clazz);
        }
        return t;
    }

    /**
     * 注册自定义 bean
     *
     * @param name     自定义 bean name
     * @param beanName 原 bean name
     */
    public static <T> T registerBean(String name, String beanName) {
        if (applicationContext.containsBean(name)) {
            throw new RuntimeException("BeanName 重复 " + name);
        }
        DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinition beanDefinition = new ChildBeanDefinition(beanName);
        acf.registerBeanDefinition(name, beanDefinition);
        return getBean(name);
    }

    /**
     * 自动注入依赖
     *
     * @param existingBean 需要自动注入依赖的 bean
     * @param <T>          bean
     * @return bean
     */
    public static <T> T autowireBean(T existingBean) {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(existingBean);
        return existingBean;
    }

}
