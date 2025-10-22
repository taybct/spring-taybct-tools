package io.github.taybct.tool.core.poi.easypoi.util;

import cn.afterturn.easypoi.excel.annotation.*;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.taybct.tool.core.poi.easypoi.constants.EasyPOIConstant;
import io.github.taybct.tool.core.util.StringUtil;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 数据库字段工具类
 * </pre>
 *
 * @author XiJieYin
 * @since 2024/10/10 19:24
 */
public class DBFieldUtil {

    public static <T> List<DBField> generate(Class<T> clazz) throws Exception {
        return generate(clazz, null);
    }

    public static <T> List<DBField> generate(Class<T> clazz, String groupName) throws Exception {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        if (!clazz.isAnnotationPresent(ExcelTarget.class)) {
            throw new IllegalStateException("clazz " + clazz.getName() + " must be with annotation @cn.afterturn.easypoi.excel.annotation.ExcelTarget");
        }
        Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
        T temp = declaredConstructor.newInstance();
        List<DBField> list = new ArrayList<DBField>();
        Field[] fields = clazz.getDeclaredFields();
        // 表名
        String tableName = StringUtil.humpToUnderline(clazz.getSimpleName());
        if (clazz.isAnnotationPresent(TableName.class)) {
            tableName = clazz.getAnnotation(TableName.class).value();
        }
        for (Field field : fields) {
            // 字段
            String realField = clazz.getName() + EasyPOIConstant.TABLE_FIELD_PARTITION_D + field.getName();
            // 字段名
            String fieldName = StringUtil.humpToUnderline(field.getName());
            if (field.isAnnotationPresent(TableField.class)) {
                fieldName = field.getAnnotation(TableField.class).value();
            }
            if (field.isAnnotationPresent(TableId.class)) {
                fieldName = field.getAnnotation(TableId.class).value();
            }
            String key = tableName + EasyPOIConstant.TABLE_FIELD_PARTITION + fieldName;
            if (!Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                Object fieldValue = field.get(temp);
                if (field.isAnnotationPresent(ExcelIgnore.class)) {
                    continue;
                }
                if (field.isAnnotationPresent(Excel.class)) {
                    // 先找这个类里面的所有的注解
                    Excel excel = field.getAnnotation(Excel.class);
                    if (StringUtil.isBlank(groupName)) {
                        groupName = excel.orderNum();
                    }
                    if (StringUtil.isEmpty(excel.name())) {
                        // 如果标签为空，就不导出
                        continue;
                    }
                    list.add(DBField.builder()
                            .name(excel.name())
                            // 字段查询回来的格式是：表名_字段名
                            .key(key)
                            .width(excel.width())
                            .dict(excel.dict())
                            .groupName(excel.groupName())
                            .mergeVertical(excel.mergeVertical())
                            .replace(excel.replace())
                            .orderNum(excel.orderNum())
                            .format(excel.format())
                            .mergeSame(true)
                            .needMerge(excel.needMerge())
                            .defaultValue(fieldValue)
                            .build());
                }
                if (field.isAnnotationPresent(ExcelEntity.class)) {
                    // 这里获取，关联的实体对象
                    ExcelEntity excelEntity = field.getAnnotation(ExcelEntity.class);
                    String name = excelEntity.name();
                    List<DBField> entityField = generate(field.getType(), name);
                    if (CollectionUtil.isEmpty(entityField)) {
                        throw new IllegalStateException("field " + realField + " config excel fields must not be empty");
                    }
                    list.addAll(entityField);
                }
                if (field.isAnnotationPresent(ExcelCollection.class)) {
                    // 这里获取，关联的实体对象集体
                    ExcelCollection excelCollection = field.getAnnotation(ExcelCollection.class);
                    String name = excelCollection.name();
                    if (!field.getType().isAssignableFrom(List.class)) {
                        throw new IllegalStateException("field " + realField + " type must be List");
                    }
                    // 当前集合的泛型类型
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType parameterizedType) {
                        // 得到泛型里的class类型对象
                        Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        List<DBField> collectionField = generate(actualTypeArgument, name);
                        if (CollectionUtil.isEmpty(collectionField)) {
                            throw new IllegalStateException("field " + realField + " config excel fields must not be empty");
                        }
                        list.addAll(collectionField);
                    } else {
                        throw new IllegalStateException("collection field " + realField + " type must be with generics type");
                    }
                }
            }
        }
        return list;
    }

}
