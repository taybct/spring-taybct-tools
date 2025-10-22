package io.github.taybct.tool.core.handle;

import io.github.taybct.tool.core.mybatis.util.MybatisOptional;

/**
 * <pre>
 * 查询条件处理器
 * </pre>
 *
 * @author XiJieYin
 * @since 2025/1/14 14:43
 */
public interface QueryConditionHandler<T> {
    /**
     * 查询条件
     *
     * @param condition       任何想传入的
     * @param mybatisOptional 操作选项
     * @return 任何想返回的
     */
    default Object where(Object condition, MybatisOptional<T> mybatisOptional) {
        return condition;
    }

    /**
     * 分页条件
     *
     * @param condition       任何想传入的
     * @param mybatisOptional 操作选项
     * @return 任何想返回的
     */
    default Object page(Object condition, MybatisOptional<T> mybatisOptional) {
        return condition;
    }

    /**
     * 排序条件
     *
     * @param condition       任何想传入的
     * @param mybatisOptional 操作选项
     * @return 任何想返回的
     */
    default Object sort(Object condition, MybatisOptional<T> mybatisOptional) {
        return condition;
    }

}
