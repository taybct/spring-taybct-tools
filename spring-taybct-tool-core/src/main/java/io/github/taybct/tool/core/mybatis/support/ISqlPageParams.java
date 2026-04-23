package io.github.taybct.tool.core.mybatis.support;

/**
 *
 * <pre>
 * 分布工具
 * </pre>
 *
 * @author XiJieYin
 * @since 2026/4/21 15:19
 */
public interface ISqlPageParams {

    /**
     * 获取当前页码
     *
     * @return 当前页码
     */
    Long getPageNum();

    /**
     * 获取每页数量
     *
     * @return 每页数量
     */
    Long getPageSize();

    /**
     * 是否统计总数
     *
     * @return 是否统计总数
     */
    Boolean getCountTotal();

    /**
     * 获取排序字段
     *
     * @return 排序字段
     */
    String getPageOrder();

}
