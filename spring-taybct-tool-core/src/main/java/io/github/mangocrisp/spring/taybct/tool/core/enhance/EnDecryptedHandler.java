package io.github.mangocrisp.spring.taybct.tool.core.enhance;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceElement;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceElementIgnore;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceElementMap;
import io.github.mangocrisp.spring.taybct.tool.core.annotation.EnhanceElements;
import io.github.mangocrisp.spring.taybct.tool.core.util.BeanUtil;
import io.github.mangocrisp.spring.taybct.tool.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 加解密处理器
 *
 * @author XiJieYin <br> 2024/4/19 15:13
 */
@Slf4j
public class EnDecryptedHandler implements IMethodEnhanceHandler {

    @Override
    public void before(Method method
            , String[] argumentsNames
            , Object[] arguments
            , Object aThis
            , AccessibleObject staticPart) {

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < argumentsNames.length; i++) {
            // 参数的类型
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            EnhanceElement enDecryptedElement = checkAnnotation(() -> parameterAnnotation, EnhanceElement.class);
            // 参数名
            String argumentsName = argumentsNames[i];

            // 参数值
            Object o = arguments[i];
            if (ObjectUtil.isNotEmpty(o)) {

                // 参数类型
                Class<?> cls = o.getClass();

                Object edObject = enDecrypted(argumentsName
                        , o
                        , BeanUtils.isSimpleValueType(cls)
                        , true
                        , () -> method.isAnnotationPresent(EnhanceElements.class) ? method.getAnnotation(EnhanceElements.class) : null
                        , () -> enDecryptedElement);

                arguments[i] = edObject;

            }

        }
    }

    /**
     * 获取所有的参数里面有没有指定的注解
     *
     * @param annotationSupplier 注解数组
     * @param classes            需要找到的注解
     * @param <T>                需要找的注解的类型
     * @return 找到的注解或者是 null
     */
    public <T extends Annotation> T checkAnnotation(Supplier<Annotation[]> annotationSupplier, Class<T> classes) {
        Annotation[] annotations = annotationSupplier.get();
        if (ArrayUtil.isNotEmpty(annotations)) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().isAssignableFrom(classes)) {
                    return (T) annotation;
                }
            }
        }
        return null;
    }

    @Override
    public void after(Method method
            , String[] argumentsNames
            , Object[] arguments
            , Object aThis
            , AccessibleObject staticPart
            , AtomicReference<Object> proceed) {

        Object o = proceed.get();
        if (ObjectUtil.isNotEmpty(o)) {
            Class<?> cls = o.getClass();

            Object edObject = enDecrypted(null
                    , o
                    , BeanUtils.isSimpleValueType(cls)
                    , false
                    , () -> method.isAnnotationPresent(EnhanceElements.class) ? method.getAnnotation(EnhanceElements.class) : null
                    , () -> null);
            proceed.set(edObject);
            return;
        }
        proceed.set(o);
    }

    /**
     * 增强字符串
     *
     * @param argumentsName              被处理的字符串名
     * @param str                        需要被处理的字符串值
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     * @return 返回处理后的字符中
     */
    private static Object enDecryptedStr(@Nullable String argumentsName
            , String str
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {
        Object edStr = str;
        // 获取处理方法
        Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(argumentsName
                , isParameter
                , enDecryptedMethodSupplier
                , enDecryptedElementSupplier);
        if (enDeHandlerFunctions != null) {
            for (Function<Object, Object> enDeHandlerFunction : enDeHandlerFunctions) {
                edStr = enDeHandlerFunction.apply(edStr);
            }
        }
        return edStr;
    }

    /**
     * 增强处理字符串集合
     *
     * @param argumentsName              需要被处理的参数名
     * @param collection                 被处理的值
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     * @return 返回处理后的集合
     */
    private static Collection<String> enDecryptedCollectionStr(@Nullable String argumentsName
            , Collection collection
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {

//        Collection<String> edStrCollection = new ArrayList<>();
        // 获取处理方法
        Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(argumentsName
                , isParameter
                , enDecryptedMethodSupplier
                , enDecryptedElementSupplier);

        if (enDeHandlerFunctions != null) {
            // 清空集合
            for (Object str : collection) {
                Object edStr = str;
                for (Function<Object, Object> enDeHandlerFunction : enDeHandlerFunctions) {
                    edStr = enDeHandlerFunction.apply(edStr);
                }
//                edStrCollection.add(edStr);
            }
        }
//        if (CollectionUtil.isNotEmpty(edStrCollection)) {
//            collection.clear();
//            collection.addAll(edStrCollection);
//        }
        return collection;
    }

    /**
     * 增强 Map 对象
     *
     * @param edMap                      map 对象
     * @param argumentsName              需要被获取的直接的对象的名称，比如方法上的参数，类型里面的字段
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     */
    private static void enDeMap(Map<String, Object> edMap
            , @Nullable String argumentsName
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {
        Map<String, Object> keyMap = new HashMap<>();
        Map<String, Function<Object, Object>[]> functionMap = new HashMap<>();
        getMapEnDeHandlerFunctions(edMap
                , keyMap
                , functionMap
                , argumentsName
                , isParameter
                , enDecryptedMethodSupplier
                , enDecryptedElementSupplier);
        for (String key : keyMap.keySet()) {
            Object edStr = keyMap.get(key);
            Function<Object, Object>[] enDeHandlerFunctions = functionMap.get(key);
            if (enDeHandlerFunctions != null) {
                for (Function<Object, Object> enDeHandlerFunction : enDeHandlerFunctions) {
                    edStr = enDeHandlerFunction.apply(edStr);
                }
                edMap.put(key, edStr);
            }
        }
    }


    /**
     * 增强实体类
     *
     * @param entity      实体类对象
     * @param isParameter 是否是参数，true 是参数，false 是返回结果
     */
    private static Object enDeEntity(Object entity, boolean isParameter) {

        if (ObjectUtil.isEmpty(entity)) {
            return entity;
        }

        // 如果没有 EnhanceElements 注解就直接返回！
        if (!entity.getClass().isAnnotationPresent(EnhanceElements.class)) {
            return entity;
        }

        // 返回结果
        if (!isParameter && entity instanceof IPage) {
            IPage page = (IPage) entity;
            // 如果是返回的分页对象，这里只返回分页对象里面的数据
            List edRecord = new ArrayList();
            for (Object record : page.getRecords()) {
                Object edO = enDeEntity(record, isParameter);
                edRecord.add(edO);
            }
            page.setRecords(edRecord);
            return page;
        }

        Class<?> entityClazz = entity.getClass();

        List<Field> allFields = BeanUtil.getAllFields(entityClazz);
        Method[] methods = entityClazz.getMethods();
        for (Field field : allFields) {
            if (Modifier.isStatic(field.getModifiers())) {
                // 如果是静态字段，就不管
                continue;
            }

            if (field.isAnnotationPresent(EnhanceElementIgnore.class)) {
                continue;
            }

            String fieldName = field.getName();
            Object fieldValue = null;
            // 字段类型
            Class<?> cls = field.getType();

            try {
                Method readMethod = (new PropertyDescriptor(fieldName, cls)).getReadMethod();
                fieldValue = readMethod.invoke(entity);
            } catch (Exception e) {
                log.trace(e.getMessage(), e);
            }
            field.setAccessible(true);
            try {
                if (fieldValue == null) {
                    fieldValue = field.get(entity);
                }
            } catch (Exception e) {
                log.trace(e.getMessage(), e);
            }
            if (fieldValue == null) {
                String firstUpper = StringUtil.upperFirst(fieldName);
                List<Method> methodList = Arrays.stream(methods).filter(m -> m.getName().equals("is" + firstUpper)
                                || m.getName().equals("get" + firstUpper))
                        .collect(Collectors.toList());
                Method readMethod;
                if (CollectionUtil.isNotEmpty(methodList) && (readMethod = methodList.get(0)) != null) {
                    try {
                        fieldValue = readMethod.invoke(entity);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.trace(e.getMessage(), e);
                    }
                }
            }

            Object edObject = enDecrypted(fieldName
                    , fieldValue
                    , BeanUtils.isSimpleValueType(cls)
                    , isParameter
                    , () -> entityClazz.isAnnotationPresent(EnhanceElements.class) ? entityClazz.getAnnotation(EnhanceElements.class) : null
                    , () -> field.isAnnotationPresent(EnhanceElement.class) ? field.getAnnotation(EnhanceElement.class) : null);
            try {
                (new PropertyDescriptor(fieldName, entityClazz)).getWriteMethod().invoke(entity, edObject);
            } catch (IllegalAccessException | InvocationTargetException |
                     IntrospectionException e) {
                log.trace("增强失败：" + e.getMessage(), e);
                try {
                    field.set(entity, edObject);
                } catch (IllegalAccessException e1) {
                    log.trace("增强失败：" + e1.getMessage(), e1);
                }
            }
        }
        return entity;
    }

    /**
     * 增强
     *
     * @param fieldName                  需要增强的参数/字段名(如果是返回结果这个值可以是空)
     * @param o                          需要增强的参数/字段值
     * @param isSimpleValueType          是否是简单类型
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     * @return {@code Function<Object, Object>[]} 处理方法数组，按数组顺序处理增强
     */
    private static Object enDecrypted(@Nullable String fieldName
            , Object o
            , boolean isSimpleValueType
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {
        String str;
        Map map;
        Collection collection;
        if (isSimpleValueType) {
            // 如果是简单类型
            if (o instanceof String && StringUtil.isNotBlank((str = (String) o))) {
                // 如果是字符串类型

                return enDecryptedStr(fieldName
                        , str
                        , isParameter
                        , enDecryptedMethodSupplier
                        , enDecryptedElementSupplier);

            }
        } else {

            // 如果不是简单类型可能是集合或者是 Map 之类的或者是对象
            if (o instanceof Map) {
                map = (Map) o;
                enDeMap(map
                        , fieldName
                        , isParameter
                        , enDecryptedMethodSupplier
                        , enDecryptedElementSupplier);
                return map;
            } else if (o instanceof Collection && !(collection = (Collection) o).isEmpty()) {
                // 所有的对象都是字符串
                if (collection.stream().allMatch(object -> object instanceof String && StringUtil.isNotBlank(((String) object)))) {
                    // 如果是集合类型

                    return enDecryptedCollectionStr(fieldName
                            , collection
                            , isParameter
                            , enDecryptedMethodSupplier
                            , enDecryptedElementSupplier);

                } else {
                    // 不是集合就是对象
//                    if (collection.stream().allMatch(object -> object.getClass().isAnnotationPresent(EnhanceElements.class))) {

//                    Collection<Object> edStrCollection = new ArrayList<>();

                    // 如果对象是需要增强的实体集合
                    for (Object object : collection) {
                        Object edEntity = enDeEntity(object, isParameter);
//                        edStrCollection.add(edEntity);
                    }
                    // 增强完成之后进行替换
//                    if (CollectionUtil.isNotEmpty(edStrCollection)) {
//                        collection.clear();
//                        collection.addAll(edStrCollection);
//                    }
                    return collection;
//                    }
                }
            } else {
                // 如果是对象
                return enDeEntity(o, isParameter);
            }
        }
        return o;
    }

    /**
     * 获取 map 的处理方法
     *
     * @param edMap                      原数据 map
     * @param keyMap                     存储需要增强处理的 key
     * @param argumentsName              需要被获取的直接的对象的名称，比如方法上的参数，类型里面的字段
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     * @return {@code Function<Object, Object>[]} 处理方法数组，按数组顺序处理增强
     */
    @Nullable
    private static void getMapEnDeHandlerFunctions(Map<String, Object> edMap
            , Map<String, Object> keyMap
            , Map<String, Function<Object, Object>[]> functionMap
            , @Nullable String argumentsName
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {


        EnhanceElements enDecryptedMethod = enDecryptedMethodSupplier.get();
        EnhanceElement enDecryptedElement = enDecryptedElementSupplier.get();

        // 每个参数名会用到的增强器
        Map<String, Function<Object, Object>[]> enDecryptedValueMap = new HashMap<>();
        if (enDecryptedMethod != null) {

            // 这个是最外层的处理方式，如果里面不指定默认用这个
            Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(isParameter ? enDecryptedMethod.parameterHandler()
                    : enDecryptedMethod.resultHandler());

            if (StringUtil.isBlank(argumentsName)) {
                // 如果没有指定参数/字段名，说明是返回结果了这里直接就返回方法里面的增强处理方法

                for (EnhanceElementMap enhanceElementMap : enDecryptedMethod.map()) {
                    String[] keyArr = enhanceElementMap.value();
                    Function<Object, Object>[] mapFunctions = getEnDeHandlerFunctions(enhanceElementMap.resultHandler());
                    Arrays.stream(keyArr).forEach(key -> {
                        // 循环所有的 key
                        String mapStr;
                        Object o;
                        if (StringUtil.isNotBlank(key)
                                // 指定的参数名不能为空，而且参数的值也不能为空
                                && (o = edMap.get(key)) instanceof String
                                && StringUtil.isNotBlank((mapStr = (String) o))) {
                            keyMap.put(key, mapStr);
                            functionMap.put(key, mapFunctions == null ? enDeHandlerFunctions : mapFunctions);
                        }
                    });
                }
                return;
            }

            if (enDeHandlerFunctions != null) {
                enDecryptedValueMap.put(argumentsName, enDeHandlerFunctions);
            }

            // 如果没有在参数里面找到注解,就从方法上面找
            if (enDecryptedElement == null) {
                for (EnhanceElement decryptedElement : enDecryptedMethod.enDecryptedElements()) {
                    if (decryptedElement.value().equals(argumentsName)) {
                        enDecryptedElement = decryptedElement;
                        break;
                    }
                }
            }
        }

        if (enDecryptedElement != null) {

            // 如果有增强注解

            Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(isParameter ? enDecryptedElement.parameterHandler()
                    : enDecryptedElement.resultHandler());

            for (EnhanceElementMap enhanceElementMap : enDecryptedElement.map()) {
                String[] keyArr = enhanceElementMap.value();
                Function<Object, Object>[] mapFunctions = getEnDeHandlerFunctions(isParameter ? enhanceElementMap.parameterHandler()
                        : enhanceElementMap.resultHandler());
                Arrays.stream(keyArr).forEach(key -> {
                    // 循环所有的 key
                    String mapStr;
                    Object o;
                    if (StringUtil.isNotBlank(key)
                            // 指定的参数名不能为空，而且参数的值也不能为空
                            && (o = edMap.get(key)) instanceof String
                            && StringUtil.isNotBlank((mapStr = (String) o))) {
                        keyMap.put(key, mapStr);
                        functionMap.put(key, mapFunctions == null ?
                                (enDeHandlerFunctions == null ? enDecryptedValueMap.get(argumentsName) : enDeHandlerFunctions) : mapFunctions);
                    }
                });
            }
        }
    }

    /**
     * 获取到处理方法
     *
     * @param argumentsName              需要被获取的直接的对象的名称，比如方法上的参数，类型里面的字段
     * @param isParameter                是否是参数，true 是参数，false 是返回结果
     * @param enDecryptedMethodSupplier  如何获取到 {@link EnhanceElements} 注解，比如是方法上的，或者是类型上的
     * @param enDecryptedElementSupplier 如何获取到 {@link EnhanceElement} 注解，比如是方法参数上的，或者是类型字段上的
     * @return {@code Function<Object, Object>[]} 处理方法数组，按数组顺序处理增强
     */
    @Nullable
    private static Function<Object, Object>[] getEnDeHandlerFunctions(@Nullable String argumentsName
            , boolean isParameter
            , Supplier<EnhanceElements> enDecryptedMethodSupplier
            , Supplier<EnhanceElement> enDecryptedElementSupplier) {
        EnhanceElements enDecryptedMethod = enDecryptedMethodSupplier.get();
        // 每个参数会用到的增强器
        Map<String, Function<Object, Object>[]> enDecryptedParameterMap = new HashMap<>();
        // 每个参数名会用到的增强器
        Map<String, Function<Object, Object>[]> enDecryptedValueMap = new HashMap<>();
        if (enDecryptedMethod != null) {
            if (StringUtil.isBlank(argumentsName)) {
                // 如果没有指定参数/字段名，说明是返回结果了这里直接就返回方法里面的增强处理方法

                Class<? extends Function<Object, Object>>[] classes = enDecryptedMethod.resultHandler();
                return getEnDeHandlerFunctions(classes);
            }
            for (EnhanceElement enDecryptedElement : enDecryptedMethod.enDecryptedElements()) {
                // 是哪个参数需要增强
                String key = enDecryptedElement.value();
                Class<? extends Function<Object, Object>>[] classes = isParameter ? enDecryptedElement.parameterHandler()
                        : enDecryptedElement.resultHandler();
                Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(classes);
                if (enDeHandlerFunctions != null) {
                    enDecryptedParameterMap.put(key, enDeHandlerFunctions);
                }
            }
            for (String key : enDecryptedMethod.value()) {
                Class<? extends Function<Object, Object>>[] classes = isParameter ? enDecryptedMethod.parameterHandler()
                        : enDecryptedMethod.resultHandler();
                Function<Object, Object>[] enDeHandlerFunctions = getEnDeHandlerFunctions(classes);
                if (enDeHandlerFunctions != null) {
                    enDecryptedValueMap.put(key, enDeHandlerFunctions);
                }
            }
        }
        EnhanceElement enDecryptedElement = enDecryptedElementSupplier.get();
        if (enDecryptedElement != null) {
            // 如果有增强注解
            Class<? extends Function<Object, Object>>[] classes = isParameter ? enDecryptedElement.parameterHandler()
                    : enDecryptedElement.resultHandler();
            return getEnDeHandlerFunctions(classes);
        } else if (enDecryptedParameterMap.containsKey(argumentsName)) {
            // 如果在参数/字段注解里面
            return enDecryptedParameterMap.get(argumentsName);
        } else if (enDecryptedValueMap.containsKey(argumentsName)) {
            // 如果在方法/类型注解里面
            return enDecryptedValueMap.get(argumentsName);
        }
        return null;
    }

    /**
     * 根据提供的类型获取增强的处理类，该如何处理增强
     *
     * @param classes 类型
     * @return {@code Function<Object, Object>[]} 处理方法数组，按数组顺序处理增强
     */
    @Nullable
    private static Function<Object, Object>[] getEnDeHandlerFunctions(Class<? extends Function<Object, Object>>[] classes) {
        Function<Object, Object>[] array = null;
        List<Function<Object, Object>> funList = new ArrayList<>();
        for (Class<? extends Function<Object, Object>> clazz : classes) {
            try {
                Constructor<? extends Function<Object, Object>> declaredConstructor = clazz.getDeclaredConstructor();
                Function<Object, Object> function = declaredConstructor.newInstance();
                funList.add(function);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (!funList.isEmpty()) {
            array = funList.toArray(new Function[funList.size()]);
        }
        return array;
    }

}
