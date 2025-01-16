package io.github.mangocrisp.spring.taybct.tool.core.dto;

import java.io.Serializable;

/**
 * <pre>
 * 传输数据类型
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/13 14:57
 */
public interface SyncToAnywhereDTO<P> extends Serializable {

    /**
     * <pre>
     * 唯一的名，用于做区分数据，可以理解为数据库的表名,
     * 如果这个没指定，默认就是用实体类的类名，如果实体类有相关的注解（例如 {@linkplain org.springframework.data.mongodb.core.mapping.Document Document}、
     * {@linkplain com.baomidou.mybatisplus.annotation.TableName TableName}）就会使用这些注解里面的属性
     * </pre>
     *
     * @return 字符串
     */
    default String uniqueName() {
        return null;
    }

    /**
     * 数据的主键，用于新删改
     *
     * @return 主键
     */
    P getId();

}
