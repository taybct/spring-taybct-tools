package io.github.taybct.tool.core.config;

/**
 * 自定义默认值规则
 *
 * @author XiJieYin <br> 2023/7/3 16:32
 */
public interface TableFieldDefaultHandler<T> {

    /**
     * 生成器
     *
     * @param entity 实体类
     * @return 生成结果
     */
    default T get(Object entity) {
        return null;
    }

}
