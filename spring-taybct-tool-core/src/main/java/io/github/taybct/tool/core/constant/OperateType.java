package io.github.taybct.tool.core.constant;

/**
 * 操作类型
 *
 * @author xijieyin <br> 2022/8/5 18:35
 * @since 1.0.0
 */
public interface OperateType {
    /**
     * 查询
     */
    String QUERY = "QUERY";
    /**
     * 其它
     */
    String OTHER = "OTHER";

    /**
     * 新增
     */
    String INSERT = "INSERT";

    /**
     * 修改
     */
    String UPDATE = "UPDATE";

    /**
     * 删除
     */
    String DELETE = "DELETE";

    /**
     * 授权
     */
    String GRANT = "GRANT";

    /**
     * 导出
     */
    String EXPORT = "EXPORT";

    /**
     * 导入
     */
    String IMPORT = "IMPORT";

    /**
     * 强退
     */
    String FORCE = "FORCE";

    /**
     * 清空数据
     */
    String CLEAN = "CLEAN";
}
