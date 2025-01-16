package io.github.mangocrisp.spring.taybct.tool.core.enums;

/**
 * 数据库操作类型
 *
 * @author XiJieYin <br> 2023/1/29 16:12
 */
public final class DataOperateType {
    /**
     * 其它
     */
    public static final int OTHER = -1;
    /**
     * 查询
     */
    public static final int QUERY = 0;
    /**
     * 新增
     */
    public static final int INSERT = 1;

    /**
     * 修改
     */
    public static final int UPDATE = 2;

    /**
     * 删除
     */
    public static final int DELETE = 3;

    /**
     * 清空数据
     */
    public static final int CLEAN = 4;

}
