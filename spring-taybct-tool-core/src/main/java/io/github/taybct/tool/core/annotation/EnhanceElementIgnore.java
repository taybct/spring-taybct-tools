package io.github.taybct.tool.core.annotation;

import java.lang.annotation.*;

/**
 * 类型字段忽略处理，用来忽略有些字段的处理，比如在类里面套用类来当子类的时候又不希望子类被处理，就可以使用这个注解来忽略掉
 *
 * @author XiJieYin <br> 2024/4/19 14:11
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})//参数和字段上
@Retention(RetentionPolicy.RUNTIME)//运行时有效
@Documented //标识这是个注解并应该被 javadoc工具记录
@Inherited                          //允许子类继承
public @interface EnhanceElementIgnore {
}
